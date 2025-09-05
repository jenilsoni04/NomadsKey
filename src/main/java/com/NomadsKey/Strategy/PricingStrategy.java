package com.NomadsKey.Strategy;

import com.NomadsKey.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}
