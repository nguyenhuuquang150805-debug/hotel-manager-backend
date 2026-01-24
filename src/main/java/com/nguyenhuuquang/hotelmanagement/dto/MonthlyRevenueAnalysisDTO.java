package com.nguyenhuuquang.hotelmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRevenueAnalysisDTO {
    private Integer year;
    private Integer month;
    private Double roomRevenue;
    private Double serviceRevenue;
    private Double totalRevenue;
    private Long checkoutCount;
    private Double avgRevenuePerCheckout;
    private Double avgNightsPerStay;
}