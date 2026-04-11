package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.entities.Booking;

public interface CheckOutService {

    String getCheckOutSession(Booking booking, String successUrl, String failureUrl);
}
