package com.project.airBnbApp.controller;

import com.project.airBnbApp.dto.BookingDto;
import com.project.airBnbApp.dto.BookingRequest;
import com.project.airBnbApp.dto.GuestDto;
import com.project.airBnbApp.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {
private final BookingService bookingService;


    @PostMapping("/init")
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest){
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId,
            @RequestBody List<GuestDto> guestDtoList){

         return ResponseEntity.ok(bookingService.addGuests(bookingId,guestDtoList));
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String, String>> initiatePayments(@PathVariable Long bookingId){

        String sessionUrl= bookingService.initiatePayments(bookingId) ;

        return ResponseEntity.ok(Map.of("sessionUrl",sessionUrl));
    }



}
