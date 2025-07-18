package com.airbnb.airbnb.controller;


import com.airbnb.airbnb.dto.RoomDto;
import com.airbnb.airbnb.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createnewroom(@PathVariable Long hotelId,@RequestBody RoomDto roomdto)
    {
        RoomDto room=roomService.createRoom(hotelId,roomdto);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getallroominhotel(@PathVariable Long hotelId)
    {
        return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
    }
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getroombyid(roomId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<RoomDto> deleteRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        roomService.deleteroombyid(roomId);
        return ResponseEntity.noContent().build();
    }
}
