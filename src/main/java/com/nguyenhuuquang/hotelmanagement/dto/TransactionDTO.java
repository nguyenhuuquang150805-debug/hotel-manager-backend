package com.nguyenhuuquang.hotelmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long id;

    @NotBlank(message = "Loại giao dịch không được để trống")
    private String type;

    @NotBlank(message = "Danh mục không được để trống")
    private String category;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Ngày giao dịch không được để trống")
    private LocalDate transactionDate;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    private Long bookingId;
    private LocalDateTime createdAt;
}