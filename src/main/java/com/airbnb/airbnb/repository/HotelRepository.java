package com.airbnb.airbnb.repository;


import com.airbnb.airbnb.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository <Hotel,Long>{

}
