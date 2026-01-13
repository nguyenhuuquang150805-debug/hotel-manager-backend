package com.nguyenhuuquang.hotelmanagement.service;

import java.math.BigDecimal;
import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.BookingServiceDTO;

public interface RoomServiceManager {

    List<BookingServiceDTO> getServicesByBooking(Long bookingId);

    BookingServiceDTO addServiceToBooking(Long bookingId, Long serviceId,
            Integer quantity, String notes,
            String username);

    BookingServiceDTO updateServiceQuantity(Long bookingServiceId,
            Integer quantity, String notes);

    void removeServiceFromBooking(Long bookingServiceId);

    BigDecimal calculateTotalServiceAmount(Long bookingId);

    BookingServiceDTO getBookingServiceById(Long id);

    List<Object[]> getMostUsedServices();
}