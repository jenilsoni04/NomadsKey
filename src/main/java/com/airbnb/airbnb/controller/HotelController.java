package com.airbnb.airbnb.controller;

import com.airbnb.airbnb.dto.HotelDto;
import com.airbnb.airbnb.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;
    @PostMapping
    public ResponseEntity<HotelDto> createhotel(@RequestBody HotelDto hotelDto)
    {
        log.info("Attempting to create a new hotel with name"+hotelDto.getName());
        HotelDto hotel=hotelService.creatennewhotel(hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }
    @GetMapping("/{hotelid}")
    public ResponseEntity<HotelDto> gethotel(@PathVariable Long hotelid)
    {
        HotelDto hotelDto=hotelService.gethotelbyid(hotelid);
        return ResponseEntity.ok(hotelDto);
    }
    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updatehotelbyid(@PathVariable Long hotelId,@RequestBody HotelDto hotelDto)
    {
        HotelDto hotel=hotelService.updatehotelbyid(hotelId,hotelDto);
        return ResponseEntity.ok(hotel);
    }
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<HotelDto> deletehotelbyid(@PathVariable Long hotelId)
    {
        hotelService.deletehotelbyid(hotelId);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{hotelid}/activate")
    public ResponseEntity<Void> activatehotel(@PathVariable Long hotelid)
    {
        hotelService.activateHotel(hotelid);
        return ResponseEntity.noContent().build();
    }

}
