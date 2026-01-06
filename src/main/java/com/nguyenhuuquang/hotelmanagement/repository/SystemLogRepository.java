package com.nguyenhuuquang.hotelmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenhuuquang.hotelmanagement.entity.SystemLog;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findByType(LogType type);

    List<SystemLog> findByUsername(String username);

    List<SystemLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
}