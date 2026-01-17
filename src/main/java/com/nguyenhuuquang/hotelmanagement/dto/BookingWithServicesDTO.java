package com.nguyenhuuquang.hotelmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingWithServicesDTO {
    private Long id;
    private Long roomId;
    private String roomNumber;
    private String customerName;
    private String phone;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer nights;
    private BigDecimal roomAmount;
    private BigDecimal serviceAmount;
    private BigDecimal totalAmount;
    private BigDecimal deposit;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private List<BookingServiceItemDTO> services;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingServiceItemDTO {
        private Long id;
        private Long serviceId;
        private String serviceName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;
    }
}