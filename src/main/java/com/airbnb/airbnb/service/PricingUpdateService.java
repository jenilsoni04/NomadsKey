package com.airbnb.airbnb.service;

import com.airbnb.airbnb.Strategy.PricingService;
import com.airbnb.airbnb.entity.Hotel;
import com.airbnb.airbnb.entity.HotelMinPrice;
import com.airbnb.airbnb.entity.Inventory;
import com.airbnb.airbnb.repository.HotelMinPriceRepository;
import com.airbnb.airbnb.repository.HotelRepository;
import com.airbnb.airbnb.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService
{
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

//    @Scheduled(cron = "0 0 * * * *")
    @Scheduled(cron = "0/5 * * * * *")
    public void updatePrices()
    {
        int page=0;
        int batchsize=100;

        while(true)
        {
            Page<Hotel> hotelPage=hotelRepository.findAll(PageRequest.of(page,batchsize));
            if(hotelPage.isEmpty())
            {
                break;
            }
            hotelPage.getContent().forEach(this::updatehotelprices);
            page++;
        }

    }
    private void updatehotelprices(Hotel hotel)
    {
        log.info("Updating hotel prices for hotel ID: {}", hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);
        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);
        updateInventoryPrices(inventoryList);
        endDate=LocalDate.now().plusDays(90);
        inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);
        updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyMinPrices.entrySet()) {
            LocalDate date = entry.getKey();
            BigDecimal price = entry.getValue();
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        }

        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList)
    {
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }
}
