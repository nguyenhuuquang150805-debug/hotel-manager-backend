package com.nguyenhuuquang.hotelmanagement.service;

import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.BookingDTO;
import com.nguyenhuuquang.hotelmanagement.dto.BookingWithServicesDTO;
import com.nguyenhuuquang.hotelmanagement.dto.CreateBookingRequest;
import com.nguyenhuuquang.hotelmanagement.dto.DashboardStatsDTO;

public interface BookingService {
    BookingDTO createBooking(CreateBookingRequest request);

    BookingDTO confirmBooking(Long bookingId);

    BookingDTO checkIn(Long bookingId);

    BookingDTO checkOut(Long bookingId);

    BookingDTO completeBooking(Long bookingId);

    void cancelBooking(Long bookingId);

    void markNoShow(Long bookingId);

    List<BookingDTO> getActiveBookings();

    List<BookingDTO> getAllBookings();

    List<BookingDTO> getBookingsByStatus(String status);

    BookingDTO getBookingById(Long id);

    BookingWithServicesDTO getBookingWithServices(Long id);

    DashboardStatsDTO getBookingStats();

    BookingDTO addServiceToBooking(Long bookingId, Long serviceId, Integer quantity);
}