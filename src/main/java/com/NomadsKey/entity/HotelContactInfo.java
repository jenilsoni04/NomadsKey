package com.NomadsKey.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class HotelContactInfo {

    private String address;
    private String phonenumber;
    private String email;
    private String location;
}
