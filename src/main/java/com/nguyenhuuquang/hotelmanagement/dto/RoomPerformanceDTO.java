package com.nguyenhuuquang.hotelmanagement.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomPerformanceDTO {
    private String roomNumber;
    private Long totalBookings;
    private Long totalNights;
    private Double totalRoomRevenue;
    private Double totalServiceRevenue;
    private Double totalRevenue;
    private Double avgRevenuePerBooking;
    private Double avgNightsPerBooking;
    private LocalDate firstCheckout;
    private LocalDate lastCheckout;
}