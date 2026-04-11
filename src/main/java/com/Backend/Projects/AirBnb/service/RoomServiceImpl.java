package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.RoomDto;
import com.Backend.Projects.AirBnb.entities.Hotel;
import com.Backend.Projects.AirBnb.entities.Room;
import com.Backend.Projects.AirBnb.entities.User;
import com.Backend.Projects.AirBnb.exceptions.ResourceNotFoundException;
import com.Backend.Projects.AirBnb.exceptions.UnAuthorizedException;
import com.Backend.Projects.AirBnb.repository.HotelRepository;
import com.Backend.Projects.AirBnb.repository.InventoryRepository;
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
public class RoomServiceImpl implements RoomService {


    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;


    @Override
    @Transactional
    public RoomDto createRoom(Long hotelId,RoomDto roomDto) {
        log.info("creating a room in hotel with id  {}",hotelId);
        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow( () -> new ResourceNotFoundException("hotel not found with the id:"+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthorizedException("This User with userId "+user.getId()+" is not The owner of this hotel with hotelId  "+hotelId);
        }

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);
        if(hotel.getIsActive()){
            inventoryService.InitialiseInventory(room);
        }
        log.info("Room created successfully  with ID {}", room.getId());
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public RoomDto getRoomById(Long hotelId, Long roomId) {
        log.info("Getting room with id {} in hotel {}", roomId, hotelId);

        // Fetch the hotel
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));


        // Fetch the room that belongs to this hotel
        Room room = roomRepository.findById(roomId)
                .filter(r -> r.getHotel().getId().equals(hotel.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id " + roomId + " for hotel id " + hotelId));

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRooms(Long hotelId) {
        log.info("getting all the rooms in hotel with id  {}",hotelId);
        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow( () -> new ResourceNotFoundException("hotel not found with the id:"+hotelId));


       return hotel.getRooms()
               .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthorizedException("This User with userId "+user.getId()+" is not The owner of this hotel with hotelId "+room.getHotel().getId());
        }

        inventoryService.deleteAllInventory(room);
        log.info("Inventory for Room  with ID {} is successfully deleted", roomId);
        roomRepository.deleteById(roomId);
        log.info("Room deleted successfully  with ID {}", roomId);

    }

    @Override
    @Transactional
    public RoomDto updateRoom(Long hotelId, Long roomId, RoomDto roomDto) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("hotel not found with the id:" + hotelId));

        User user =getCurrentUser();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthorizedException("This User with userId " + user.getId() + " is not The owner of this hotel with hotelId  " + hotelId);
        }
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomId));

        modelMapper.map(roomDto, room);
        room.setId(roomId);
        room = roomRepository.save(room);

        return modelMapper.map(room, RoomDto.class);
    }
}
