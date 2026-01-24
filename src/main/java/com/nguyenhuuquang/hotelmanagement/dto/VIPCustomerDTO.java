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
public class VIPCustomerDTO {
    private String customerName;
    private String phone;
    private Long visitCount;
    private Double totalSpent;
    private Double avgSpentPerVisit;
    private Long totalNights;
    private LocalDate firstVisit;
    private LocalDate lastVisit;
}