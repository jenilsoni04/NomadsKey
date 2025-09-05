package com.NomadsKey.service;

import com.NomadsKey.dto.ProfileUpdateRequestDto;
import com.NomadsKey.dto.UserDto;
import com.NomadsKey.entity.User;

public interface UserService {
    User getUserById(Long userId);
    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
