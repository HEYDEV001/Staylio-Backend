package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.ProfileResponseDto;
import com.Backend.Projects.AirBnb.dto.UpdateProfileDto;
import com.Backend.Projects.AirBnb.dto.UserDto;
import com.Backend.Projects.AirBnb.entities.User;

public interface UserService {
    User getUserById(Long userId);

    void updateProfile(UpdateProfileDto updateProfileDto);

    ProfileResponseDto getMyProfile();
}
