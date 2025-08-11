package com.airbnb.airbnb.dto;

import com.airbnb.airbnb.entity.enums.GENDER;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private GENDER gender;
    private LocalDate dateOfBirth;
}
