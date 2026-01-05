package com.nguyenhuuquang.hotelmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private Long id;
    private String name;
    private String category;
    private Integer quantity;
    private String unit;
    private Integer minStock;
    private BigDecimal price;
    private LocalDateTime lastUpdated;
}