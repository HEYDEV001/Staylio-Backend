package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.BookingDto;
import com.Backend.Projects.AirBnb.dto.BookingRequest;
import com.Backend.Projects.AirBnb.dto.GuestDto;
import com.Backend.Projects.AirBnb.dto.HotelReportDto;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingDto initializeBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayment(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBooking();
}
