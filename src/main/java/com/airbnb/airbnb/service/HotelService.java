package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.HotelDto;
import com.airbnb.airbnb.dto.HotelInfoDto;
import com.airbnb.airbnb.dto.HotelInfoRequestDto;
import com.airbnb.airbnb.entity.Hotel;

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
