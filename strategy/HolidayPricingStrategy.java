package com.Backend.Projects.AirBnb.strategy;

import com.Backend.Projects.AirBnb.entities.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;


    @Override
    public BigDecimal calculatePrice(Inventory inventory) {

        BigDecimal price  =wrapped.calculatePrice(inventory);
        boolean isHoliday=true;
        if(isHoliday){
            price = price.multiply(BigDecimal.valueOf(1.0));
        }
        return price;

    }
}
