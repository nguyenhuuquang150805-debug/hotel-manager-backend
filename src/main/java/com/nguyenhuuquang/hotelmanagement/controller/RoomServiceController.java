package com.nguyenhuuquang.hotelmanagement.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenhuuquang.hotelmanagement.dto.BookingServiceDTO;
import com.nguyenhuuquang.hotelmanagement.service.RoomServiceManager;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý dịch vụ phòng
 * Endpoint: /api/room-services (tránh trùng với /api/bookings)
 */
@RestController
@RequestMapping("/api/room-services")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomServiceController {

    private final RoomServiceManager roomServiceManager;

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingServiceDTO>> getServicesByBooking(
            @PathVariable Long bookingId) {
        List<BookingServiceDTO> services = roomServiceManager.getServicesByBooking(bookingId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingServiceDTO> getBookingServiceById(
            @PathVariable Long id) {
        BookingServiceDTO service = roomServiceManager.getBookingServiceById(id);
        return ResponseEntity.ok(service);
    }

    @PostMapping
    public ResponseEntity<BookingServiceDTO> addServiceToBooking(
            @Valid @RequestBody AddServiceRequest request,
            Principal principal) {
        BookingServiceDTO result = roomServiceManager.addServiceToBooking(
                request.getBookingId(),
                request.getServiceId(),
                request.getQuantity(),
                request.getNotes(),
                principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingServiceDTO> updateServiceQuantity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request) {
        BookingServiceDTO result = roomServiceManager.updateServiceQuantity(
                id,
                request.getQuantity(),
                request.getNotes());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeServiceFromBooking(@PathVariable Long id) {
        roomServiceManager.removeServiceFromBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/booking/{bookingId}/total")
    public ResponseEntity<BigDecimal> calculateTotalServiceAmount(
            @PathVariable Long bookingId) {
        BigDecimal total = roomServiceManager.calculateTotalServiceAmount(bookingId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/statistics/most-used")
    public ResponseEntity<List<Object[]>> getMostUsedServices() {
        List<Object[]> services = roomServiceManager.getMostUsedServices();
        return ResponseEntity.ok(services);
    }
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class AddServiceRequest {

    @jakarta.validation.constraints.NotNull(message = "Booking ID không được để trống")
    private Long bookingId;

    @jakarta.validation.constraints.NotNull(message = "Service ID không được để trống")
    private Long serviceId;

    @jakarta.validation.constraints.NotNull(message = "Số lượng không được để trống")
    @jakarta.validation.constraints.Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    private String notes;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class UpdateServiceRequest {

    @jakarta.validation.constraints.Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    private String notes;
}