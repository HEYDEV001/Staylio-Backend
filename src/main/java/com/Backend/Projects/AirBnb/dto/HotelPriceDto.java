package com.Backend.Projects.AirBnb.dto;

import com.Backend.Projects.AirBnb.entities.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDto {

    private Hotel hotel;

    private Double price;

}
