package com.nguyenhuuquang.hotelmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyPromotionRequest {

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    private String promotionCode;
}