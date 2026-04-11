package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.ProfileResponseDto;
import com.Backend.Projects.AirBnb.dto.UpdatedProfileDto;
import com.Backend.Projects.AirBnb.entities.User;

import java.util.Map;

public interface UserService {
    User getUserById(Long userId);


    ProfileResponseDto getMyProfile();

    UpdatedProfileDto updateProfile(Map<String, Object> updates);
}
