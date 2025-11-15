package com.project.airBnbApp.controller;

import com.project.airBnbApp.dto.HotelDto;
import com.project.airBnbApp.dto.HotelInfoDto;
import com.project.airBnbApp.dto.HotelPriceDto;
import com.project.airBnbApp.dto.HotelSearchRequest;
import com.project.airBnbApp.entity.Hotel;
import com.project.airBnbApp.service.HotelService;
import com.project.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {


    private final HotelService hotelService;
    private final InventoryService inventoryService;
    @GetMapping(path="/search")
    private ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){

       var page=inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }



    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId){


        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }


}
