package com.NomadsKey.Strategy;

import com.NomadsKey.entity.Inventory;

import java.math.BigDecimal;

public class BasePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
       return inventory.getRoom().getBaseprice();
    }
}
