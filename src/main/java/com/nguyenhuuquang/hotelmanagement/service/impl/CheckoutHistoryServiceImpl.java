package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.CheckoutHistoryDTO;
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
                .totalAmount(historyDTO.getTotalAmount())
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