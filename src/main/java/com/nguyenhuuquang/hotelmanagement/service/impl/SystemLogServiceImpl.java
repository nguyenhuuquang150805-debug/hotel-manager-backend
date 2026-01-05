package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.SystemLogDTO;
import com.nguyenhuuquang.hotelmanagement.entity.SystemLog;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.repository.SystemLogRepository;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository logRepo;

    @Override
    @Transactional
    public void log(LogType type, String action, String user, String description) {
        log(type, action, user, description, null);
    }

    @Override
    @Transactional
    public void log(LogType type, String action, String user, String description, String details) {
        SystemLog log = SystemLog.builder()
                .type(type)
                .action(action)
                .user(user)
                .description(description)
                .details(details)
                .build();

        logRepo.save(log);
    }

    @Override
    public List<SystemLogDTO> getAllLogs() {
        return logRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemLogDTO> getLogsByType(LogType type) {
        return logRepo.findByType(type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemLogDTO> getLogsByUser(String user) {
        return logRepo.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemLogDTO> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return logRepo.findByTimestampBetween(startDateTime, endDateTime)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SystemLogDTO convertToDTO(SystemLog log) {
        return SystemLogDTO.builder()
                .id(log.getId())
                .type(log.getType().name())
                .action(log.getAction())
                .user(log.getUser())
                .description(log.getDescription())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}