package com.nguyenhuuquang.hotelmanagement.service;

import java.time.LocalDate;
import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.CheckoutHistoryDTO;

public interface CheckoutHistoryService {
    CheckoutHistoryDTO createHistory(CheckoutHistoryDTO historyDTO);

    List<CheckoutHistoryDTO> getAllHistory();

    CheckoutHistoryDTO getHistoryByBookingId(Long bookingId);

    List<CheckoutHistoryDTO> getHistoryByDateRange(LocalDate startDate, LocalDate endDate);

    void deleteHistory(Long id);
}