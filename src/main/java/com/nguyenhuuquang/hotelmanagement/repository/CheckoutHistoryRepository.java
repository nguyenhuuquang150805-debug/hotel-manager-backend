package com.nguyenhuuquang.hotelmanagement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nguyenhuuquang.hotelmanagement.entity.CheckoutHistory;

@Repository
public interface CheckoutHistoryRepository extends JpaRepository<CheckoutHistory, Long> {
    Optional<CheckoutHistory> findByBookingId(Long bookingId);

    List<CheckoutHistory> findByActualCheckOutBetween(LocalDate startDate, LocalDate endDate);

    // 1. PHÂN TÍCH DOANH THU THEO THÁNG
    @Query("SELECT EXTRACT(YEAR FROM h.actualCheckOut) as year, " +
            "EXTRACT(MONTH FROM h.actualCheckOut) as month, " +
            "SUM(h.roomAmount) as roomRevenue, " +
            "SUM(h.serviceAmount) as serviceRevenue, " +
            "SUM(h.totalAmount) as totalRevenue, " +
            "COUNT(h) as checkoutCount, " +
            "AVG(h.totalAmount) as avgRevenuePerCheckout, " +
            "AVG(h.nights) as avgNightsPerStay " +
            "FROM CheckoutHistory h " +
            "WHERE h.actualCheckOut BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(YEAR FROM h.actualCheckOut), EXTRACT(MONTH FROM h.actualCheckOut) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyRevenueAnalysis(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 2. TOP KHÁCH HÀNG VIP
    @Query("SELECT h.customerName, " +
            "h.phone, " +
            "COUNT(h) as visitCount, " +
            "SUM(h.totalAmount) as totalSpent, " +
            "AVG(h.totalAmount) as avgSpentPerVisit, " +
            "SUM(h.nights) as totalNights, " +
            "MIN(h.actualCheckOut) as firstVisit, " +
            "MAX(h.actualCheckOut) as lastVisit " +
            "FROM CheckoutHistory h " +
            "GROUP BY h.customerName, h.phone " +
            "HAVING COUNT(h) >= :minVisits AND SUM(h.totalAmount) >= :minSpending " +
            "ORDER BY SUM(h.totalAmount) DESC, COUNT(h) DESC")
    List<Object[]> findVIPCustomers(@Param("minVisits") Long minVisits,
            @Param("minSpending") Double minSpending);

    // 3. PHÂN TÍCH HIỆU SUẤT PHÒNG
    @Query("SELECT h.roomNumber, " +
            "COUNT(h) as totalBookings, " +
            "SUM(h.nights) as totalNights, " +
            "SUM(h.roomAmount) as totalRoomRevenue, " +
            "SUM(h.serviceAmount) as totalServiceRevenue, " +
            "SUM(h.totalAmount) as totalRevenue, " +
            "AVG(h.totalAmount) as avgRevenuePerBooking, " +
            "AVG(h.nights) as avgNightsPerBooking, " +
            "MIN(h.actualCheckOut) as firstCheckout, " +
            "MAX(h.actualCheckOut) as lastCheckout " +
            "FROM CheckoutHistory h " +
            "WHERE h.actualCheckOut BETWEEN :startDate AND :endDate " +
            "GROUP BY h.roomNumber " +
            "ORDER BY SUM(h.totalAmount) DESC")
    List<Object[]> getRoomPerformanceAnalysis(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}