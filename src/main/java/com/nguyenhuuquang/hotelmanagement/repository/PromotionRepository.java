package com.nguyenhuuquang.hotelmanagement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nguyenhuuquang.hotelmanagement.entity.Promotion;
import com.nguyenhuuquang.hotelmanagement.entity.enums.PromotionType;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCode(String code);

    List<Promotion> findByActive(Boolean active);

    List<Promotion> findByType(PromotionType type);

    @Query("SELECT p FROM Promotion p WHERE p.active = true " +
            "AND p.startDate <= :date AND p.endDate >= :date " +
            "AND (p.maxUsage IS NULL OR p.usedCount < p.maxUsage)")
    List<Promotion> findActivePromotions(@Param("date") LocalDate date);

    @Query("SELECT p FROM Promotion p WHERE p.code = :code " +
            "AND p.active = true " +
            "AND p.startDate <= :date AND p.endDate >= :date " +
            "AND (p.maxUsage IS NULL OR p.usedCount < p.maxUsage)")
    Optional<Promotion> findValidPromotionByCode(
            @Param("code") String code,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.active = true " +
            "AND p.startDate <= CURRENT_DATE AND p.endDate >= CURRENT_DATE")
    Long countActivePromotions();
}