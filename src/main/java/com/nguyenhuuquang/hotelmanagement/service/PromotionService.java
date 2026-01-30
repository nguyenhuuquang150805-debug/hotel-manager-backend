package com.nguyenhuuquang.hotelmanagement.service;

import java.math.BigDecimal;
import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.PromotionDTO;

public interface PromotionService {
    PromotionDTO createPromotion(PromotionDTO promotionDTO);

    PromotionDTO updatePromotion(Long id, PromotionDTO promotionDTO);

    void deletePromotion(Long id);

    PromotionDTO getPromotionById(Long id);

    PromotionDTO getPromotionByCode(String code);

    List<PromotionDTO> getAllPromotions();

    List<PromotionDTO> getActivePromotions();

    List<PromotionDTO> getPromotionsByType(String type);

    PromotionDTO togglePromotionStatus(Long id);

    BigDecimal calculateDiscount(String promotionCode, BigDecimal bookingAmount);

    PromotionDTO validatePromotionCode(String code, BigDecimal bookingAmount);
}