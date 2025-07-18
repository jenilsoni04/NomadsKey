package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.HotelDto;
import com.airbnb.airbnb.dto.HotelSearchRequestDto;
import com.airbnb.airbnb.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {
    void initializeRoomForAYear(Room room);
    void deleteallinventories(Room room);

    Page<HotelDto> searchhotels(HotelSearchRequestDto hotelSearchRequestDto);
}
