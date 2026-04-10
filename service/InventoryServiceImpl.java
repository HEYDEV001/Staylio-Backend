package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.HotelPriceDto;
import com.Backend.Projects.AirBnb.dto.HotelSearchDto;
import com.Backend.Projects.AirBnb.dto.InventoryDto;
import com.Backend.Projects.AirBnb.dto.UpdateInventoryRequestDto;
import com.Backend.Projects.AirBnb.entities.Inventory;
import com.Backend.Projects.AirBnb.entities.Room;
import com.Backend.Projects.AirBnb.entities.User;
import com.Backend.Projects.AirBnb.exceptions.ResourceNotFoundException;
import com.Backend.Projects.AirBnb.exceptions.UnAuthorizedException;
import com.Backend.Projects.AirBnb.repository.HotelMinPriceRepository;
import com.Backend.Projects.AirBnb.repository.InventoryRepository;
import com.Backend.Projects.AirBnb.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.Backend.Projects.AirBnb.util.AppUtils.getCurrentUser;


@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;

    @Override
    public void InitialiseInventory(Room room) {

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }


    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchDto hotelSearchDto) {
        log.info("trying to search hotel in {} form {} to {}", hotelSearchDto.getStartDate(), hotelSearchDto.getEndDate(), hotelSearchDto.getCity());
        Pageable pageable = PageRequest.of(hotelSearchDto.getPage(), hotelSearchDto.getSize());
        long dateCount = ChronoUnit.DAYS.between(
                hotelSearchDto.getStartDate(),
                hotelSearchDto.getEndDate()
        ) + 1;
        Page<HotelPriceDto> hotels = hotelMinPriceRepository.findHotelsWithAvailableInventories(
                hotelSearchDto.getCity(),
                hotelSearchDto.getStartDate(),
                hotelSearchDto.getEndDate(),
                hotelSearchDto.getRoomsCount(),
                dateCount,
                pageable
        );
        return hotels;


    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {

        log.info("Trying to get room with id  {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));

        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthorizedException("This User with userId " + user.getId() + " is not The owner of this hotel with hotelId " + room.getHotel().getId());
        }

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map((inventory) -> modelMapper.map(inventory, InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating all inventory by room  for the room with id  {} between date range {} - {}", roomId,
                updateInventoryRequestDto.getStartDate(), updateInventoryRequestDto.getEndDate());
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));

        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthorizedException("This User with userId " + user.getId() + " is not The owner of this hotel with hotelId " + room.getHotel().getId());
        }

        inventoryRepository.selectInventoryAndLockBeforeUpdate(roomId,
                updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId,
                updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(),
                updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor());

    }

    @Override
    public void deleteAllInventory(Room room) {
        log.info("Deleting all inventory for the room with id {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }
}
