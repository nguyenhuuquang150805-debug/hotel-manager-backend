package com.nguyenhuuquang.hotelmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UpdateBookingServiceRequest {

    private Integer quantity;
    private String notes;
}
