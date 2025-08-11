package com.airbnb.airbnb.repository;

import com.airbnb.airbnb.entity.Booking;
import com.airbnb.airbnb.entity.Hotel;
import com.airbnb.airbnb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository <Booking,Long>{
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Booking> findByUser(User user);

    Booking[] findAllByGuestId(Long guestId);

    List<Booking> findAllByRoomId(Long roomId);
}
