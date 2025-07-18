package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.HotelDto;
import com.airbnb.airbnb.dto.HotelInfoDto;
import com.airbnb.airbnb.entity.Hotel;

public interface HotelService {
    HotelDto creatennewhotel(HotelDto hotelDto);
    HotelDto gethotelbyid(Long id);
    HotelDto updatehotelbyid(Long id,HotelDto hotelDto);
    void  deletehotelbyid(Long id);
    void activateHotel(Long hotelId);

    HotelInfoDto gethotelinfobyid(Long hotelId);
//    void getallhotels();
}
