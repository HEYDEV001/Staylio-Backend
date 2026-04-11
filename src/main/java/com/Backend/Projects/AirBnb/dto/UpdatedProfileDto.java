package com.Backend.Projects.AirBnb.dto;

import com.Backend.Projects.AirBnb.entities.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatedProfileDto {

    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;


}
