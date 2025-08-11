package com.airbnb.airbnb.dto;

import com.airbnb.airbnb.entity.enums.GENDER;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {
    private String name;
    private LocalDate dateOfBirth;
    private GENDER gender;
}
