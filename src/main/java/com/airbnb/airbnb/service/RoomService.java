package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.RoomDto;

import java.util.List;

public interface RoomService {
    RoomDto createRoom(Long HotelId,RoomDto roomDto);
    List<RoomDto> getAllRoomsInHotel(Long hotelId);
    RoomDto getroombyid(Long id);
    void deleteroombyid(Long id);

    RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto);
}
