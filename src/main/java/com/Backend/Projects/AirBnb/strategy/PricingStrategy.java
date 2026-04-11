package com.Backend.Projects.AirBnb.strategy;

import com.Backend.Projects.AirBnb.entities.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
