package com.NomadsKey.dto;

import com.NomadsKey.entity.enums.GENDER;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {
    private String name;
    private LocalDate dateOfBirth;
    private GENDER gender;
}
