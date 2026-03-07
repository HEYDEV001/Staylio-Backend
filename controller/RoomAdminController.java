package com.Backend.Projects.AirBnb.controller;

import com.Backend.Projects.AirBnb.dto.RoomDto;
import com.Backend.Projects.AirBnb.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@Slf4j
@RequiredArgsConstructor
public class RoomAdminController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(@PathVariable Long hotelId, @RequestBody RoomDto roomDto) {
        log.info("attempting to create room in the hotel with id {}", hotelId);
        RoomDto room = roomService.createRoom(hotelId, roomDto);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms(@PathVariable Long hotelId) {
        log.info("attempting to get all the rooms in the hotel with id {}", hotelId);
        return ResponseEntity.ok(roomService.getAllRooms(hotelId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        log.info("attempting to get room with id {}", roomId);
        return ResponseEntity.ok(roomService.getRoomById(hotelId, roomId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        log.info("attempting to delete room with id {}", roomId);
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roomId}/update")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long hotelId, @PathVariable Long roomId, @RequestBody RoomDto roomDto) {
        log.info("attempting to update room with id {}", roomId);
       return ResponseEntity.ok(roomService.updateRoom(hotelId, roomId, roomDto));
    }
}
