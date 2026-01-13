package com.nguyenhuuquang.hotelmanagement.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickBookingRequest {
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer numberOfGuests = 1;
    private String notes;
}