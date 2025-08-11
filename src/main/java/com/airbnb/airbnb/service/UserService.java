package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.ProfileUpdateRequestDto;
import com.airbnb.airbnb.dto.UserDto;
import com.airbnb.airbnb.entity.User;

public interface UserService {
    User getUserById(Long userId);
    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
