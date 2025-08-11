package com.airbnb.airbnb.utils;

import com.airbnb.airbnb.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {
    public static User getCurrentUser() {

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
