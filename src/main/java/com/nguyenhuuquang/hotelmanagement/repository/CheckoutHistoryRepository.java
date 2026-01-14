package com.nguyenhuuquang.hotelmanagement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenhuuquang.hotelmanagement.entity.CheckoutHistory;

@Repository
public interface CheckoutHistoryRepository extends JpaRepository<CheckoutHistory, Long> {
    Optional<CheckoutHistory> findByBookingId(Long bookingId);

    List<CheckoutHistory> findByActualCheckOutBetween(LocalDate startDate, LocalDate endDate);
}