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

    @GetMapping("/seed-data")
    public ResponseEntity<String> seedSampleDataGet() {
        return seedSampleData();
    }

    @PostMapping("/seed-data")
    public ResponseEntity<String> seedSampleDataPost() {
        return seedSampleData();
    }

    // PRIVATE METHOD CHUNG CHO CẢ GET VÀ POST
    private ResponseEntity<String> seedSampleData() {
        try {
            LocalDate today = LocalDate.now();

            // Tạo 50 checkout history từ 6 tháng trước đến hôm nay
            for (int i = 0; i < 50; i++) {
                LocalDate checkOutDate = today.minusDays((long) (Math.random() * 180));
                LocalDate checkInDate = checkOutDate.minusDays((long) (Math.random() * 5 + 1));
                int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);

                String[] customers = { "Nguyễn Văn A", "Trần Thị B", "Lê Văn C", "Phạm Thị D", "Hoàng Văn E" };
                String[] phones = { "0901234567", "0912345678", "0923456789", "0934567890", "0945678901" };
                String[] rooms = { "101", "102", "103", "201", "202", "203", "301", "302" };

                int customerIdx = (int) (Math.random() * customers.length);
                double roomPrice = 300000 + (Math.random() * 200000); // 300k-500k/đêm
                double roomAmount = roomPrice * nights;
                double serviceAmount = Math.random() * 500000; // 0-500k dịch vụ
                double deposit = roomAmount * 0.3; // cọc 30%

                CheckoutHistoryDTO dto = CheckoutHistoryDTO.builder()
                        .bookingId((long) (i + 1))
                        .roomNumber(rooms[(int) (Math.random() * rooms.length)])
                        .customerName(customers[customerIdx])
                        .phone(phones[customerIdx])
                        .checkIn(checkInDate)
                        .checkOut(checkOutDate)
                        .actualCheckOut(checkOutDate)
                        .nights(nights)
                        .roomAmount(roomAmount)
                        .serviceAmount(serviceAmount)
                        .deposit(deposit)
                        .notes("Sample checkout data #" + (i + 1))
                        .build();

                historyService.createHistory(dto);
            }

            return ResponseEntity.ok("✅ Đã tạo 50 checkout history mẫu thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Lỗi khi tạo dữ liệu: " + e.getMessage());
        }
    }
}