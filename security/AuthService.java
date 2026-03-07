package com.Backend.Projects.AirBnb.security;

import com.Backend.Projects.AirBnb.dto.LoginRequestDto;
import com.Backend.Projects.AirBnb.dto.SignUpRequestDto;
import com.Backend.Projects.AirBnb.dto.UserDto;
import com.Backend.Projects.AirBnb.entities.User;
import com.Backend.Projects.AirBnb.entities.enums.Role;
import com.Backend.Projects.AirBnb.exceptions.ResourceNotFoundException;
import com.Backend.Projects.AirBnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserDto signUp(SignUpRequestDto signUpRequestDto) {
        log.info("Sign Up Request: {}", signUpRequestDto);
        log.info("Trying to SignUp");
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
        if (user != null) {
            throw new RuntimeException("User already exists with this email Id");
        }
        log.info("User does not exist with this email Id");
        log.info("Creating new User with the email id {}",signUpRequestDto.getEmail());
        User newUser = modelMapper.map(signUpRequestDto, User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser = userRepository.save(newUser);
        return modelMapper.map(newUser, UserDto.class);
    }

    public String[] login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDto.getEmail(), loginRequestDto.getPassword()));

        User user = (User) authentication.getPrincipal();
        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);

        return arr;
    }

    public String refresh(String refreshToken){
        long id = jwtService.getIdFromTheToken(refreshToken);
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User does not exist with Id : "+id)
        );
        return jwtService.generateAccessToken(user);
    }

}
