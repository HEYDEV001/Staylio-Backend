package com.Backend.Projects.AirBnb.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelSearchDto {

    private String city;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer roomsCount;

    private Integer page=0;

    private Integer size =10;

}
