package com.nguyenhuuquang.hotelmanagement.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenhuuquang.hotelmanagement.dto.SystemLogDTO;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemLogController {

    private final SystemLogService logService;

    @GetMapping
    public ResponseEntity<List<SystemLogDTO>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SystemLogDTO>> getLogsByType(@PathVariable String type) {
        LogType logType = LogType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(logService.getLogsByType(logType));
    }

    @GetMapping("/user/{user}")
    public ResponseEntity<List<SystemLogDTO>> getLogsByUser(@PathVariable String user) {
        return ResponseEntity.ok(logService.getLogsByUser(user));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SystemLogDTO>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(logService.getLogsByDateRange(startDate, endDate));
    }
}