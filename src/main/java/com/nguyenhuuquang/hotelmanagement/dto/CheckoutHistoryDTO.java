package com.nguyenhuuquang.hotelmanagement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutHistoryDTO {
    private Long id;
    private Long bookingId;
    private String roomNumber;
    private String customerName;
    private String phone;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private LocalDate actualCheckOut;
    private Integer nights;
    private Double roomAmount;
    private Double serviceAmount;
    private Double totalAmount;
    private Double deposit;
    private String notes;
    private LocalDateTime createdAt;
}