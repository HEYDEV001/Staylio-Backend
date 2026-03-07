package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.HotelPriceDto;
import com.Backend.Projects.AirBnb.dto.HotelSearchDto;
import com.Backend.Projects.AirBnb.dto.InventoryDto;
import com.Backend.Projects.AirBnb.dto.UpdateInventoryRequestDto;
import com.Backend.Projects.AirBnb.entities.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    void InitialiseInventory(Room room);
    void deleteAllInventory(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchDto hotelSearchDto);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
