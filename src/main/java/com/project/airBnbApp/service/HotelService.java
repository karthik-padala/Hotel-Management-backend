package com.project.airBnbApp.service;

import com.project.airBnbApp.dto.HotelDto;
import com.project.airBnbApp.dto.HotelInfoDto;
import com.project.airBnbApp.entity.Hotel;

import java.util.List;

public interface HotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id,HotelDto hotelDto);

    public void deleteHotelById(Long id );

    public void activateHotel(Long hotelId);

    List<HotelDto> getAllHotels();

    HotelInfoDto getHotelInfoById(Long hotelId);
}
