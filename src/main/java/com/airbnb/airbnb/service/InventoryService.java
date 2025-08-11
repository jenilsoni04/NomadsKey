package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.*;
import com.airbnb.airbnb.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    void initializeRoomForAYear(Room room);
    void deleteallinventories(Room room);

    Page<HotelPriceDto> searchhotels(HotelSearchRequestDto hotelSearchRequestDto);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(java.lang.Long roomId, com.airbnb.airbnb.dto.UpdateInventoryRequestDto updateInventoryRequestDto);
}
