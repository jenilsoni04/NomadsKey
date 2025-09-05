package com.NomadsKey.dto;

import com.NomadsKey.entity.enums.GENDER;
import com.NomadsKey.entity.enums.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SignUpRequestDto {
    private String email;
    private String password;
    private String name;
    private LocalDate dateofbirth;
    private GENDER gender;
    private Role role;
}
