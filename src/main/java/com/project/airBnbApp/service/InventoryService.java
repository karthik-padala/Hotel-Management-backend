package com.project.airBnbApp.service;

import com.project.airBnbApp.dto.HotelDto;
import com.project.airBnbApp.dto.HotelPriceDto;
import com.project.airBnbApp.dto.HotelSearchRequest;
import com.project.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {
    void initializeRoomForAYear(Room room);
    void deleteAllInventories(Room room);


    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
