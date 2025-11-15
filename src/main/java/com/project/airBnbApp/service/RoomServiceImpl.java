package com.project.airBnbApp.service;
import com.project.airBnbApp.dto.RoomDto;
import com.project.airBnbApp.entity.Hotel;
import com.project.airBnbApp.entity.Room;
import com.project.airBnbApp.entity.User;
import com.project.airBnbApp.exception.ResourceNotFoundException;
import com.project.airBnbApp.exception.UnAuthorizedException;
import com.project.airBnbApp.repository.HotelRepository;
import com.project.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Slf4j

public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;


    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with Id: {}",hotelId);
        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id :"+hotelId));

        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id :"+hotelId );
        }

        Room room =modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room= roomRepository.save(room);
        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(room);

        }
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("getting all rooms in the hotel with Id: {}",hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID:"+hotelId));

        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id :"+hotelId );
        }

        return hotel.getRooms()
                .stream()
                .map((element)->modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("getting the room with Id: {}",roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("room not found with ID:"+roomId));

        return modelMapper.map(room,RoomDto.class);
    }

    @Transactional
    @Override
    public void deleteRoomById(Long roomId) {
        log.info("deleting the room with Id: {}",roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("room not found with ID:"+roomId));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel with id :"+roomId );
        }


        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
    }
}
