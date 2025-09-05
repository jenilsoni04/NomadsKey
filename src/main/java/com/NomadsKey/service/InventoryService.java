package com.NomadsKey.service;

import com.NomadsKey.dto.HotelPriceDto;
import com.NomadsKey.dto.HotelSearchRequestDto;
import com.NomadsKey.dto.InventoryDto;
import com.NomadsKey.dto.UpdateInventoryRequestDto;
import com.airbnb.airbnb.dto.*;
import com.NomadsKey.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    void initializeRoomForAYear(Room room);
    void deleteallinventories(Room room);

    Page<HotelPriceDto> searchhotels(HotelSearchRequestDto hotelSearchRequestDto);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(java.lang.Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
