package com.nguyenhuuquang.hotelmanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenhuuquang.hotelmanagement.dto.BookingDTO;
import com.nguyenhuuquang.hotelmanagement.dto.BookingWithServicesDTO;
import com.nguyenhuuquang.hotelmanagement.dto.CreateBookingRequest;
import com.nguyenhuuquang.hotelmanagement.dto.DashboardStatsDTO;
import com.nguyenhuuquang.hotelmanagement.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingDTO booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PostMapping("/{id}/services")
    public ResponseEntity<BookingDTO> addService(
            @PathVariable Long id,
            @RequestParam Long serviceId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(bookingService.addServiceToBooking(id, serviceId, quantity));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<BookingWithServicesDTO> getBookingWithServices(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingWithServices(id));
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<BookingDTO>> getActiveBookings() {
        return ResponseEntity.ok(bookingService.getActiveBookings());
    }

    @GetMapping("/status")
    public ResponseEntity<List<BookingDTO>> getBookingsByStatus(@RequestParam String status) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @PutMapping("/{id}/checkin")
    public ResponseEntity<BookingDTO> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.checkIn(id));
    }

    @PutMapping("/{id}/checkout")
    public ResponseEntity<BookingDTO> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.checkOut(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingDTO> completeBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.completeBooking(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/noshow")
    public ResponseEntity<Void> markNoShow(@PathVariable Long id) {
        bookingService.markNoShow(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getBookingStats() {
        return ResponseEntity.ok(bookingService.getBookingStats());
    }

    @PutMapping("/{id}/change-room")
    public ResponseEntity<BookingDTO> changeRoom(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        Long newRoomId = request.get("newRoomId");
        if (newRoomId == null) {
            throw new IllegalArgumentException("newRoomId không được để trống");
        }
        return ResponseEntity.ok(bookingService.changeRoom(id, newRoomId));
    }
}