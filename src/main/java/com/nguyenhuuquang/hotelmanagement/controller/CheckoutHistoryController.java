package com.nguyenhuuquang.hotelmanagement.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenhuuquang.hotelmanagement.dto.CheckoutHistoryDTO;
import com.nguyenhuuquang.hotelmanagement.dto.MonthlyRevenueAnalysisDTO;
import com.nguyenhuuquang.hotelmanagement.dto.RoomPerformanceDTO;
import com.nguyenhuuquang.hotelmanagement.dto.VIPCustomerDTO;
import com.nguyenhuuquang.hotelmanagement.service.CheckoutHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkout-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CheckoutHistoryController {

    private final CheckoutHistoryService historyService;

    @PostMapping
    public ResponseEntity<CheckoutHistoryDTO> createHistory(@RequestBody CheckoutHistoryDTO historyDTO) {
        CheckoutHistoryDTO created = historyService.createHistory(historyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CheckoutHistoryDTO>> getAllHistory() {
        return ResponseEntity.ok(historyService.getAllHistory());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<CheckoutHistoryDTO> getHistoryByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(historyService.getHistoryByBookingId(bookingId));
    }

    @GetMapping("/range")
    public ResponseEntity<List<CheckoutHistoryDTO>> getHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(historyService.getHistoryByDateRange(startDate, endDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        historyService.deleteHistory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics/monthly-revenue")
    public ResponseEntity<List<MonthlyRevenueAnalysisDTO>> getMonthlyRevenueAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(historyService.getMonthlyRevenueAnalysis(startDate, endDate));
    }

    @GetMapping("/analytics/vip-customers")
    public ResponseEntity<List<VIPCustomerDTO>> getVIPCustomers(
            @RequestParam(defaultValue = "2") Long minVisits,
            @RequestParam(defaultValue = "1000000") Double minSpending) {
        return ResponseEntity.ok(historyService.getVIPCustomers(minVisits, minSpending));
    }

    @GetMapping("/analytics/room-performance")
    public ResponseEntity<List<RoomPerformanceDTO>> getRoomPerformanceAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(historyService.getRoomPerformanceAnalysis(startDate, endDate));
    }
}