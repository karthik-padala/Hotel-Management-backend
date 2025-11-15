package com.project.airBnbApp.strategy;

import com.project.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);





}
