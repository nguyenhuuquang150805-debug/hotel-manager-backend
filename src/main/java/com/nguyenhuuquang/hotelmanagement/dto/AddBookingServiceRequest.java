package com.nguyenhuuquang.hotelmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AddBookingServiceRequest {

    private Long bookingId;
    private Long serviceId;
    private Integer quantity;
    private String notes;
}