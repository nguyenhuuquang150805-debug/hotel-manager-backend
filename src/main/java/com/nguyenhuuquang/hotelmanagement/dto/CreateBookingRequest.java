package com.nguyenhuuquang.hotelmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    private Long roomId;
    private String customerName;
    private String phone;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal deposit;
    private String notes;

    private String promotionCode;
}