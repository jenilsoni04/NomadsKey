package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.HotelDto;
import com.airbnb.airbnb.dto.HotelSearchRequestDto;
import com.airbnb.airbnb.entity.Hotel;
import com.airbnb.airbnb.entity.Inventory;
import com.airbnb.airbnb.entity.Room;
import com.airbnb.airbnb.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class  InventoryServiceImpl implements InventoryService{
    private final ModelMapper modelMapper;
private final InventoryRepository inventoryRepository;
    @Override
    public void initializeRoomForAYear(Room room) {

        LocalDate today=LocalDate.now();
        LocalDate endDate=today.plusYears(1);
        for (; !today.isAfter(endDate);
             today=today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookCount(0)
                    .ReservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBaseprice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalcount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
    }
        }


    @Override
    public void deleteallinventories(Room room) {
        LocalDate today = LocalDate.now();
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelDto> searchhotels(HotelSearchRequestDto hotelSearchRequestDto) {
        log.info("Searching hotels for {} city, from {} to {}", hotelSearchRequestDto.getCity(), hotelSearchRequestDto.getStartDate(), hotelSearchRequestDto.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequestDto.getPage(), hotelSearchRequestDto.getSize());
        long dateCount =
                ChronoUnit.DAYS.between(hotelSearchRequestDto.getStartDate(), hotelSearchRequestDto.getEndDate()) + 1;

        Page<Hotel> hotelPage = inventoryRepository.findHotelsWithAvailableInventory(hotelSearchRequestDto.getCity(),
                hotelSearchRequestDto.getStartDate(), hotelSearchRequestDto.getEndDate(), hotelSearchRequestDto.getRoomsCount(),
                dateCount, pageable);

        return hotelPage.map((element) -> modelMapper.map(element, HotelDto.class));
    }
}
