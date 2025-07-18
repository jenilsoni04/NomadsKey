package com.airbnb.airbnb.dto;

import com.airbnb.airbnb.entity.User;
import com.airbnb.airbnb.entity.enums.GENDER;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private GENDER gender;
    private Integer age;
}
