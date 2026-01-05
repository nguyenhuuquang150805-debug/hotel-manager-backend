package com.nguyenhuuquang.hotelmanagement.service;

import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.BookingDTO;
import com.nguyenhuuquang.hotelmanagement.dto.CreateBookingRequest;
import com.nguyenhuuquang.hotelmanagement.dto.DashboardStatsDTO;

public interface BookingService {
    BookingDTO createBooking(CreateBookingRequest request);

    void checkOut(Long bookingId);

    void cancelBooking(Long bookingId);

    List<BookingDTO> getActiveBookings();

    List<BookingDTO> getAllBookings();

    BookingDTO getBookingById(Long id);

    DashboardStatsDTO getBookingStats();
}