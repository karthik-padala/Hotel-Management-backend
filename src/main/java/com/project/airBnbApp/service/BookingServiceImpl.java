package com.project.airBnbApp.service;

import com.project.airBnbApp.dto.BookingDto;
import com.project.airBnbApp.dto.BookingRequest;
import com.project.airBnbApp.dto.GuestDto;
import com.project.airBnbApp.entity.*;
import com.project.airBnbApp.entity.enums.BookingStatus;
import com.project.airBnbApp.exception.ResourceNotFoundException;
import com.project.airBnbApp.exception.UnAuthorizedException;
import com.project.airBnbApp.repository.*;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;

    private final BookingRepository bookingRepository;
    private final CheckoutService checkoutService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        log.info("Initializing booking for hotel: {}, room: {}, date {} - {}", bookingRequest.getHotelId(), bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(() ->
                new ResourceNotFoundException("hotel not found with id:" + bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(() ->
                new ResourceNotFoundException("room not found with id:" + bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        log.info("the inventoryList size is :{}", inventoryList.size());
        log.info("inventoryList to string : {}", inventoryList.toString());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;

        log.info("daysCount: {}", daysCount);
        if (inventoryList.size() != daysCount) {
            throw new IllegalStateException(("Room is not available anymore"));
        }

        //Reserve the room/ update the booked count of inventories

        for (Inventory inventory : inventoryList) {
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        //Create the Booking


        //TODO: calculate dynamic pricing


        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(BigDecimal.valueOf(100))
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {


        log.info("Adding guests for booking id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("booking not not found with id:" + bookingId));

       User user= getCurrentUser();
       if(!user.equals(booking.getUser())){
          throw new UnAuthorizedException("Booking doesnot belong to this user with id: "+user.getId());
       }


        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");

        }
        if (booking.getBookingStatus() != BookingStatus.RESERVED)
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");

        for (GuestDto guestDto : guestDtoList) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);

        }
        booking.setBookingStatus((BookingStatus.GUESTS_ADDED));
        booking= bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);

    }






    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking not found with id :"+ bookingId)
        );

       User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking doesnot belong to this user with id: "+user.getId());
        }


        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");

        }

       String sessionUrl=checkoutService.getCheckoutSession(booking,frontendUrl+"/payments/success",frontendUrl+"/payments/failure");

         booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
         bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            log.info("the session is : {} ",session);
            if (session == null) return;
            log.info("executing the capture payments -1");
            String sessionId = session.getId();


            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() ->
                            new ResourceNotFoundException("Booking not found for session ID: "+sessionId));
            log.info("the booking id : {}",booking.getId());

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            log.info("set to confirmed : {}",booking.getBookingStatus());
            bookingRepository.save(booking);
            log.info("saved to repository");

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }
    }



    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}