package com.airbnb.airbnb.Strategy;

import com.airbnb.airbnb.PricingStrategy;
import com.airbnb.airbnb.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price=wrapped.calculatePrice(inventory);
        double occupancyrate=inventory.getBookCount()/inventory.getTotalCount();
        if(occupancyrate > 0.8)
        {
            price=price.multiply(BigDecimal.valueOf(1.2));
        }
        return price;
    }
}
