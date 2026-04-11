package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.RoomDto;

import java.util.List;

public interface RoomService {
    RoomDto createRoom(Long hotelId, RoomDto roomDto);
    RoomDto getRoomById(Long hotelId, Long roomId);
    List<RoomDto> getAllRooms(Long hotelId);
    void deleteRoomById(Long roomId);

    RoomDto updateRoom(Long hotelId, Long roomId, RoomDto roomDto);
}
