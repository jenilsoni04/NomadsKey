package com.NomadsKey.controller;

import com.NomadsKey.dto.HotelInfoDto;
import com.NomadsKey.dto.HotelInfoRequestDto;
import com.NomadsKey.dto.HotelPriceDto;
import com.NomadsKey.dto.HotelSearchRequestDto;
import com.airbnb.airbnb.dto.*;
import com.NomadsKey.service.HotelService;
import com.NomadsKey.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;
    @GetMapping("/search")
    @Operation(summary = "Search hotels", tags = {"Browse Hotels"})
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequestDto hotelSearchRequest) {

        var page = inventoryService.searchhotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    @Operation(summary = "Get a hotel info by hotelId", tags = {"Browse Hotels"})
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId, @RequestBody HotelInfoRequestDto hotelInfoRequestDto) {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId, hotelInfoRequestDto));
    }

}
