package com.Backend.Projects.AirBnb.controller;

import com.Backend.Projects.AirBnb.dto.BookingDto;
import com.Backend.Projects.AirBnb.dto.ProfileResponseDto;
import com.Backend.Projects.AirBnb.dto.UpdatedProfileDto;
import com.Backend.Projects.AirBnb.service.BookingService;
import com.Backend.Projects.AirBnb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    @GetMapping("/myProfile")
    public ResponseEntity<ProfileResponseDto> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PatchMapping("/profile")
    public ResponseEntity<UpdatedProfileDto> updateProfile(@RequestBody Map<String,Object> updates){
        return ResponseEntity.ok(userService.updateProfile(updates));
    }

    @GetMapping("myBooking")
    public ResponseEntity<List<BookingDto>> getMyBooking(){
      return ResponseEntity.ok(bookingService.getMyBooking());
    }
}
