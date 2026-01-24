package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.CheckoutHistoryDTO;
import com.nguyenhuuquang.hotelmanagement.dto.MonthlyRevenueAnalysisDTO;
import com.nguyenhuuquang.hotelmanagement.dto.RoomPerformanceDTO;
import com.nguyenhuuquang.hotelmanagement.dto.VIPCustomerDTO;
import com.nguyenhuuquang.hotelmanagement.entity.CheckoutHistory;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.CheckoutHistoryRepository;
import com.nguyenhuuquang.hotelmanagement.service.CheckoutHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutHistoryServiceImpl implements CheckoutHistoryService {

    private final CheckoutHistoryRepository historyRepo;

    @Override
    @Transactional
    public CheckoutHistoryDTO createHistory(CheckoutHistoryDTO historyDTO) {
        Double calculatedTotal = historyDTO.getRoomAmount()
                + historyDTO.getServiceAmount()
                - historyDTO.getDeposit();

        CheckoutHistory history = CheckoutHistory.builder()
                .bookingId(historyDTO.getBookingId())
                .roomNumber(historyDTO.getRoomNumber())
                .customerName(historyDTO.getCustomerName())
                .phone(historyDTO.getPhone())
                .checkIn(historyDTO.getCheckIn())
                .checkOut(historyDTO.getCheckOut())
                .actualCheckOut(historyDTO.getActualCheckOut())
                .nights(historyDTO.getNights())
                .roomAmount(historyDTO.getRoomAmount())
                .serviceAmount(historyDTO.getServiceAmount())
                .totalAmount(calculatedTotal)
                .deposit(historyDTO.getDeposit())
                .notes(historyDTO.getNotes())
                .build();

        CheckoutHistory saved = historyRepo.save(history);
        return convertToDTO(saved);
    }

    @Override
    public List<CheckoutHistoryDTO> getAllHistory() {
        return historyRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CheckoutHistoryDTO getHistoryByBookingId(Long bookingId) {
        CheckoutHistory history = historyRepo.findByBookingId(bookingId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy lịch sử cho booking ID: " + bookingId));
        return convertToDTO(history);
    }

    @Override
    public List<CheckoutHistoryDTO> getHistoryByDateRange(LocalDate startDate, LocalDate endDate) {
        return historyRepo.findByActualCheckOutBetween(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteHistory(Long id) {
        if (!historyRepo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy lịch sử với ID: " + id);
        }
        historyRepo.deleteById(id);
    }

    @Override
    public List<MonthlyRevenueAnalysisDTO> getMonthlyRevenueAnalysis(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = historyRepo.getMonthlyRevenueAnalysis(startDate, endDate);

        return results.stream()
                .map(row -> MonthlyRevenueAnalysisDTO.builder()
                        .year((Integer) row[0])
                        .month((Integer) row[1])
                        .roomRevenue((Double) row[2])
                        .serviceRevenue((Double) row[3])
                        .totalRevenue((Double) row[4])
                        .checkoutCount((Long) row[5])
                        .avgRevenuePerCheckout((Double) row[6])
                        .avgNightsPerStay((Double) row[7])
                        .build())
                .collect(Collectors.toList());
    }

    // 2. TÌM KHÁCH HÀNG VIP
    @Override
    public List<VIPCustomerDTO> getVIPCustomers(Long minVisits, Double minSpending) {
        List<Object[]> results = historyRepo.findVIPCustomers(minVisits, minSpending);

        return results.stream()
                .map(row -> VIPCustomerDTO.builder()
                        .customerName((String) row[0])
                        .phone((String) row[1])
                        .visitCount((Long) row[2])
                        .totalSpent((Double) row[3])
                        .avgSpentPerVisit((Double) row[4])
                        .totalNights((Long) row[5])
                        .firstVisit((LocalDate) row[6])
                        .lastVisit((LocalDate) row[7])
                        .build())
                .collect(Collectors.toList());
    }

    // 3. PHÂN TÍCH HIỆU SUẤT PHÒNG
    @Override
    public List<RoomPerformanceDTO> getRoomPerformanceAnalysis(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = historyRepo.getRoomPerformanceAnalysis(startDate, endDate);

        return results.stream()
                .map(row -> RoomPerformanceDTO.builder()
                        .roomNumber((String) row[0])
                        .totalBookings((Long) row[1])
                        .totalNights((Long) row[2])
                        .totalRoomRevenue((Double) row[3])
                        .totalServiceRevenue((Double) row[4])
                        .totalRevenue((Double) row[5])
                        .avgRevenuePerBooking((Double) row[6])
                        .avgNightsPerBooking((Double) row[7])
                        .firstCheckout((LocalDate) row[8])
                        .lastCheckout((LocalDate) row[9])
                        .build())
                .collect(Collectors.toList());
    }

    private CheckoutHistoryDTO convertToDTO(CheckoutHistory history) {
        return CheckoutHistoryDTO.builder()
                .id(history.getId())
                .bookingId(history.getBookingId())
                .roomNumber(history.getRoomNumber())
                .customerName(history.getCustomerName())
                .phone(history.getPhone())
                .checkIn(history.getCheckIn())
                .checkOut(history.getCheckOut())
                .actualCheckOut(history.getActualCheckOut())
                .nights(history.getNights())
                .roomAmount(history.getRoomAmount())
                .serviceAmount(history.getServiceAmount())
                .totalAmount(history.getTotalAmount())
                .deposit(history.getDeposit())
                .notes(history.getNotes())
                .createdAt(history.getCreatedAt())
                .build();
    }

}