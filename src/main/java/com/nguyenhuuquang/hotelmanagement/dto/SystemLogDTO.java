package com.nguyenhuuquang.hotelmanagement.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDTO {
    private Long id;
    private String type;
    private String action;
    private String user;
    private String description;
    private String details;
    private LocalDateTime timestamp;
}