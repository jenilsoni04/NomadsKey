package com.NomadsKey.service;

import com.NomadsKey.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
