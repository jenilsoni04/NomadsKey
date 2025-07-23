package com.airbnb.airbnb.controller;

import com.airbnb.airbnb.dto.HotelDto;
import com.airbnb.airbnb.dto.HotelInfoDto;
import com.airbnb.airbnb.dto.HotelPriceDto;
import com.airbnb.airbnb.dto.HotelSearchRequestDto;
import com.airbnb.airbnb.service.HotelService;
import com.airbnb.airbnb.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;
    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequestDto hotelSearchRequest) {

        var page = inventoryService.searchhotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> gethotelinfo(@PathVariable Long hotelId)
    {

        return  ResponseEntity.ok(hotelService.gethotelinfobyid(hotelId));
    }

}
