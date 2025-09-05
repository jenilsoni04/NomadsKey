package com.NomadsKey.repository;

import com.NomadsKey.entity.Guest;
import com.NomadsKey.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
}
