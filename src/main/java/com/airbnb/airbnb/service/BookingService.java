package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.BookingDto;
import com.airbnb.airbnb.dto.BookingRequest;
import com.airbnb.airbnb.dto.GuestDto;
import com.airbnb.airbnb.dto.HotelReportDto;
import com.airbnb.airbnb.entity.enums.BookingStatus;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<Long> guestIdList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    BookingStatus getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBookings();
}
