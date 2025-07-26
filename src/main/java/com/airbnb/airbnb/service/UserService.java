package com.airbnb.airbnb.service;

import com.airbnb.airbnb.entity.User;

public interface UserService {
    User getUserById(Long userId);
}
