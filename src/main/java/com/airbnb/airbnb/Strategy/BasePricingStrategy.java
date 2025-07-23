package com.airbnb.airbnb.Strategy;

import com.airbnb.airbnb.PricingStrategy;
import com.airbnb.airbnb.entity.Inventory;

import java.math.BigDecimal;

public class BasePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
       return inventory.getRoom().getBaseprice();
    }
}
