package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.ProfileResponseDto;
import com.Backend.Projects.AirBnb.dto.UpdatedProfileDto;
import com.Backend.Projects.AirBnb.entities.User;
import com.Backend.Projects.AirBnb.exceptions.ResourceNotFoundException;
import com.Backend.Projects.AirBnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;

import static com.Backend.Projects.AirBnb.util.AppUtils.getCurrentUser;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("User Not found with Email: " + username));
    }

    private final UserRepository userRepository;

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not fount with id : " +userId));
    }

    @Override
    public UpdatedProfileDto updateProfile(Map<String,Object> updates) {
        User user = getCurrentUser();
        updates.forEach((field, value) -> {
            Field fieldToBeUpdated = ReflectionUtils.findField(User.class, field);
            fieldToBeUpdated.setAccessible(true);

            Class<?> fieldType = fieldToBeUpdated.getType();

            if (fieldType.equals(LocalDate.class) && value instanceof String) {
                value = LocalDate.parse((String) value);
            }
            if (fieldType.isEnum() && value instanceof String) {
                value = Enum.valueOf((Class<Enum>) fieldType, (String) value);
            }
            ReflectionUtils.setField(fieldToBeUpdated, user, value);
        });
        userRepository.save(user);
        return modelMapper.map(user, UpdatedProfileDto.class);
    }

    @Override
    public ProfileResponseDto getMyProfile() {
        User user = getCurrentUser();
        log.info("getting the profile for the user with ID : " + user.getId());
        return modelMapper.map(user, ProfileResponseDto.class);
    }
}
