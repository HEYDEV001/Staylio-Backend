package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.HotelDto;
import com.Backend.Projects.AirBnb.dto.HotelInfoDto;
import com.Backend.Projects.AirBnb.dto.RoomDto;
import com.Backend.Projects.AirBnb.entities.Hotel;
import com.Backend.Projects.AirBnb.entities.Room;
import com.Backend.Projects.AirBnb.entities.User;
import com.Backend.Projects.AirBnb.exceptions.ResourceNotFoundException;
import com.Backend.Projects.AirBnb.exceptions.UnAuthorizedException;
import com.Backend.Projects.AirBnb.repository.HotelRepository;
import com.Backend.Projects.AirBnb.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.Backend.Projects.AirBnb.util.AppUtils.getCurrentUser;


@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {


    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;


    @Override
    public HotelDto createHotel(HotelDto hotelDto) {
        log.info("creating a hotel with name {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setIsActive(false);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);

        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Hotel created successfully  with ID {}", savedHotel.getId());
        return modelMapper.map(savedHotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("getting hotel with id {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with the id:" + id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthorizedException("This User with userId " + user.getId() + " is not The owner of this hotel with hotelId  " + id);
        }

        return modelMapper.map(hotel, HotelDto.class);
    }


    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with the id:" + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthorizedException("This User with userId " + user.getId() + " is not The owner of this hotel with hotelId  " + id);
        }

        modelMapper.map(hotelDto, hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        log.info("Hotel updated successfully  with ID {}", hotel.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user = getCurrentUser();
        log.info("getting all hotels for the User with UserId{}", user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);

        return hotels.stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelInfoDto getHotelInfo(Long hotelId) {
        log.info("getting information of hotel with id {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with the id:" + hotelId));
        log.info("getting the rooms for the Hotel with id {}", hotelId);
        List<RoomDto> rooms = hotel.getRooms().stream()
                .map(element -> modelMapper.map(element, RoomDto.class))
                .toList();
        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        log.info("Getting hotel with id {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with the id:" + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthorizedException("This User with userId " + user.getId() + " is not The owner of this hotel with hotelId " + id);
        }

        log.info("deleting Inventory and rooms for the hotel with id {}", id);
        for (Room room : hotel.getRooms()) {
            inventoryService.deleteAllInventory(room);
            roomRepository.deleteById(room.getId());
        }
        log.info("Inventory and rooms are deleted successfully of the hotel with ID {}", id);
        log.info("Deleting hotel with id {}", id);
        hotelRepository.deleteById(id);
        log.info("Hotel deleted successfully  with ID {}", id);

    }

    @Override
    public void activateHotel(Long id) {
        log.info("Activating hotel with id {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with the id:" + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthorizedException("This User with userId " + user.getId() + "is not The owner of this hotel with hotelId  " + id);
        }

        hotel.setIsActive(true);
        hotelRepository.save(hotel);
        for (Room room : hotel.getRooms()) {
            inventoryService.InitialiseInventory(room);
        }
        log.info("Hotel activated successfully  with ID {}", id);
    }


}


