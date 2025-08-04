package com.airbnb.airbnb.service;

import com.airbnb.airbnb.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
