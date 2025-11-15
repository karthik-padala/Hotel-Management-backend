package com.project.airBnbApp.service;

import com.project.airBnbApp.dto.HotelDto;
import com.project.airBnbApp.dto.HotelInfoDto;
import com.project.airBnbApp.dto.RoomDto;
import com.project.airBnbApp.entity.Hotel;
import com.project.airBnbApp.entity.Room;
import com.project.airBnbApp.entity.User;
import com.project.airBnbApp.exception.ResourceNotFoundException;
import com.project.airBnbApp.exception.UnAuthorizedException;
import com.project.airBnbApp.repository.HotelRepository;
import com.project.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("creating a new hotel with name:"+hotelDto.getName());
        Hotel hotel= modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);

        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);

        hotel=hotelRepository.save(hotel);
        log.info("created a new hotel with ID: {}",hotelDto.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("getting the hotel with Id: {}",id);
        Hotel hotel= hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with Id: "+id));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel");
        }

    return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("updating the hotel with id {}",id);
        Hotel hotel= hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with Id: "+id));

        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel");
        }


        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel= hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel= hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with Id: "+ id));

        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel");
        }


        for(Room room: hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);

    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the hotel with id {}",hotelId);
        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with Id: "+ hotelId));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel");
        }

       hotel.setActive(true);
       //assuming only do it only once
        for(Room room: hotel.getRooms()){
           inventoryService.initializeRoomForAYear(room);
        }

    }

    //i have separately added

    @Override
    public List<HotelDto> getAllHotels() {
        List<Hotel> hotels=hotelRepository.findAll();
         return hotels.stream()
                 .map(hotel->modelMapper.map(hotel,HotelDto.class))
                 .collect(Collectors.toList());
    }


    //public method
    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {

        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with Id: "+ hotelId));

        List<RoomDto> rooms=hotel.getRooms()
                .stream()
                .map(room->modelMapper.map(room,RoomDto.class))
                .toList();

          return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);

    }
}
