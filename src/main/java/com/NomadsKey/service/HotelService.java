package com.NomadsKey.service;

import com.NomadsKey.dto.HotelDto;
import com.NomadsKey.dto.HotelInfoDto;
import com.NomadsKey.dto.HotelInfoRequestDto;

import java.util.List;

public interface HotelService {
    HotelDto creatennewhotel(HotelDto hotelDto);
    HotelDto gethotelbyid(Long id);
    HotelDto updatehotelbyid(Long id,HotelDto hotelDto);
    void  deletehotelbyid(Long id);
    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto);

    List<HotelDto> getAllHotels();

}
