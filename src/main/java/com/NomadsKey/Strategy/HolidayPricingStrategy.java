package com.NomadsKey.Strategy;

import com.NomadsKey.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    private static final List<LocalDate> HOLIDAYS = List.of(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 26),
            LocalDate.of(2025, 8, 15),
            LocalDate.of(2025, 10, 2),
            LocalDate.of(2025, 12, 25),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 8, 15),
            LocalDate.of(2026, 10, 2),
            LocalDate.of(2026, 12, 25)
    );
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
       BigDecimal price=wrapped.calculatePrice(inventory);
        LocalDate today = LocalDate.now();
        if (HOLIDAYS.contains(today)) {
            price = price.multiply(BigDecimal.valueOf(1.25));
        }
        return price;
    }
}
