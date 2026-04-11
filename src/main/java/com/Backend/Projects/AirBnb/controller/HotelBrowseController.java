package com.Backend.Projects.AirBnb.controller;

import com.Backend.Projects.AirBnb.dto.HotelInfoDto;
import com.Backend.Projects.AirBnb.dto.HotelPriceDto;
import com.Backend.Projects.AirBnb.dto.HotelSearchDto;
import com.Backend.Projects.AirBnb.service.HotelService;
import com.Backend.Projects.AirBnb.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchDto hotelSearchDto) {
        Page<HotelPriceDto> page = inventoryService.searchHotels(hotelSearchDto);
        return ResponseEntity.ok(page);

    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelInfo(hotelId));
    }

}
