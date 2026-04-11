package com.Backend.Projects.AirBnb.dto;

import com.Backend.Projects.AirBnb.entities.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestDto {

    private Long userId;

    private String name;

    private Gender gender;

    private Integer age;
}
