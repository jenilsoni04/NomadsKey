package com.airbnb.airbnb.dto;

import com.airbnb.airbnb.entity.enums.Role;
import lombok.Data;

@Data
public class SignUpRequestDto {
    private String email;
    private String password;
    private String name;
    private Role role;
}
