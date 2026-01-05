package com.nguyenhuuquang.hotelmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long todayRentals;
    private Long waitingRooms;
    private Long occupiedRooms;
    private Long cleaningRooms;
    private Long totalRooms;
    private Long availableRooms;
}