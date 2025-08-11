package com.airbnb.airbnb.repository;

import com.airbnb.airbnb.entity.Guest;
import com.airbnb.airbnb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
}
