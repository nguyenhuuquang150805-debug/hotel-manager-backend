package com.nguyenhuuquang.hotelmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nguyenhuuquang.hotelmanagement.entity.BookingPromotion;

@Repository
public interface BookingPromotionRepository extends JpaRepository<BookingPromotion, Long> {

    List<BookingPromotion> findByBookingId(Long bookingId);

    List<BookingPromotion> findByPromotionId(Long promotionId);

    @Query("SELECT bp FROM BookingPromotion bp WHERE bp.booking.id = :bookingId " +
            "AND bp.appliedAt BETWEEN :startDate AND :endDate")
    List<BookingPromotion> findByBookingIdAndDateRange(
            @Param("bookingId") Long bookingId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(bp) FROM BookingPromotion bp WHERE bp.promotion.id = :promotionId")
    Long countUsageByPromotionId(@Param("promotionId") Long promotionId);

    @Query("SELECT bp.promotion.code, bp.promotion.name, COUNT(bp) as usage_count " +
            "FROM BookingPromotion bp " +
            "GROUP BY bp.promotion.id, bp.promotion.code, bp.promotion.name " +
            "ORDER BY usage_count DESC")
    List<Object[]> findMostUsedPromotions();
}