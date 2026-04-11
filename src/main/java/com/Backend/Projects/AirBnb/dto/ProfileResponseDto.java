package com.Backend.Projects.AirBnb.dto;

import com.Backend.Projects.AirBnb.entities.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileResponseDto {
    private Long id;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;

}
