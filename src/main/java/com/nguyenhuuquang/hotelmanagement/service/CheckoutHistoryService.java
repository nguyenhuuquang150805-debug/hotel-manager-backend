package com.nguyenhuuquang.hotelmanagement.service;

import java.time.LocalDate;
import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.CheckoutHistoryDTO;
import com.nguyenhuuquang.hotelmanagement.dto.MonthlyRevenueAnalysisDTO;
import com.nguyenhuuquang.hotelmanagement.dto.RoomPerformanceDTO;
import com.nguyenhuuquang.hotelmanagement.dto.VIPCustomerDTO;

public interface CheckoutHistoryService {
    CheckoutHistoryDTO createHistory(CheckoutHistoryDTO historyDTO);

    List<CheckoutHistoryDTO> getAllHistory();

    CheckoutHistoryDTO getHistoryByBookingId(Long bookingId);

    List<CheckoutHistoryDTO> getHistoryByDateRange(LocalDate startDate, LocalDate endDate);

    void deleteHistory(Long id);

    List<MonthlyRevenueAnalysisDTO> getMonthlyRevenueAnalysis(LocalDate startDate, LocalDate endDate);

    List<VIPCustomerDTO> getVIPCustomers(Long minVisits, Double minSpending);

    List<RoomPerformanceDTO> getRoomPerformanceAnalysis(LocalDate startDate, LocalDate endDate);
}