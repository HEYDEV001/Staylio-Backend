package com.Backend.Projects.AirBnb.strategy;

import com.Backend.Projects.AirBnb.entities.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy {
    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        double occupancy = (double) inventory.getBookedCount() / inventory.getTotalCount();
        if(occupancy > 0.8) {
            price  = price.multiply(BigDecimal.valueOf(1.0));
        }
        return price;
    }
}
