package com.Backend.Projects.AirBnb.controller;

import com.Backend.Projects.AirBnb.dto.BookingDto;
import com.Backend.Projects.AirBnb.dto.ProfileResponseDto;
import com.Backend.Projects.AirBnb.dto.UpdateProfileDto;
import com.Backend.Projects.AirBnb.dto.UserDto;
import com.Backend.Projects.AirBnb.service.BookingService;
import com.Backend.Projects.AirBnb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Void> updateProfile(@RequestBody UpdateProfileDto updateProfileDto){
        userService.updateProfile(updateProfileDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("myBooking")
    public ResponseEntity<List<BookingDto>> getMyBooking(){
      return ResponseEntity.ok(bookingService.getMyBooking());
    }
}
