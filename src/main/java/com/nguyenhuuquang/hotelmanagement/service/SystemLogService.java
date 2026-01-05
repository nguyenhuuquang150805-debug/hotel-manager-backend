package com.nguyenhuuquang.hotelmanagement.service;

import java.time.LocalDate;
import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.SystemLogDTO;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;

public interface SystemLogService {
    void log(LogType type, String action, String user, String description);

    void log(LogType type, String action, String user, String description, String details);

    List<SystemLogDTO> getAllLogs();

    List<SystemLogDTO> getLogsByType(LogType type);

    List<SystemLogDTO> getLogsByUser(String user);

    List<SystemLogDTO> getLogsByDateRange(LocalDate startDate, LocalDate endDate);
}