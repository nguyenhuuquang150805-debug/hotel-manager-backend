package com.nguyenhuuquang.hotelmanagement.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private String roomNumber;
    private String roomTypeName;
    private Integer floor;
    private BigDecimal price;
    private String status;
    private String description;
}