package com.airbnb.airbnb.service;

import com.airbnb.airbnb.entity.User;
import com.airbnb.airbnb.entity.enums.Role;
import com.airbnb.airbnb.exception.ResourceNotFoundException;
import com.airbnb.airbnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{
    private final UserRepository userRepository;
    public void approveHotelManager(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.getRole().contains(Role.PENDING_HOTEL_MANAGER)) {
            throw new IllegalStateException("User is not pending approval for hotel manager");
        }

        user.getRole().remove(Role.PENDING_HOTEL_MANAGER);
        user.getRole().add(Role.HOTEL_MANAGER);
        userRepository.save(user);
    }
}
