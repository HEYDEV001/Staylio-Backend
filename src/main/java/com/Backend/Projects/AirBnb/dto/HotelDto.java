package com.Backend.Projects.AirBnb.dto;

import com.Backend.Projects.AirBnb.entities.HotelContactInfo;
import lombok.Data;
@Data
public class HotelDto {

    private Long id;

    private String name;

    private String city;

    private String[] photos;

    private String[] amenities;

    private HotelContactInfo contactInfo;

    private Boolean isActive;
}
