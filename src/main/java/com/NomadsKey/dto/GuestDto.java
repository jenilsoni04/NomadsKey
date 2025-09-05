package com.NomadsKey.dto;

import com.NomadsKey.entity.enums.GENDER;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestDto {
    private String name;
    private GENDER gender;
    private LocalDate dateOfBirth;
}

