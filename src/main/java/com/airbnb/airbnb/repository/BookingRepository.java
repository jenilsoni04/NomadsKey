package com.airbnb.airbnb.repository;

import com.airbnb.airbnb.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository <Booking,Long>{
}
