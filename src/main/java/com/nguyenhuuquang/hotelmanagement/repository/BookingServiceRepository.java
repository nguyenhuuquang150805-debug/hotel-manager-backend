package com.nguyenhuuquang.hotelmanagement.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nguyenhuuquang.hotelmanagement.entity.BookingService;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, Long> {

    List<BookingService> findByBookingId(Long bookingId);

    List<BookingService> findByServiceId(Long serviceId);

    @Query("SELECT bs FROM BookingService bs WHERE bs.booking.id = :bookingId " +
            "AND bs.serviceDate BETWEEN :startDate AND :endDate")
    List<BookingService> findByBookingIdAndDateRange(
            @Param("bookingId") Long bookingId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(bs.totalPrice), 0) FROM BookingService bs " +
            "WHERE bs.booking.id = :bookingId")
    BigDecimal calculateTotalServiceAmount(@Param("bookingId") Long bookingId);

    @Query("SELECT COUNT(bs) FROM BookingService bs WHERE bs.booking.id = :bookingId")
    Long countServicesByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT bs.service.id, bs.service.name, COUNT(bs) as usage_count " +
            "FROM BookingService bs " +
            "GROUP BY bs.service.id, bs.service.name " +
            "ORDER BY usage_count DESC")
    List<Object[]> findMostUsedServices();
}