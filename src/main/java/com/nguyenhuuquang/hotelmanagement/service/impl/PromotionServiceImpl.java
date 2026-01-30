package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.PromotionDTO;
import com.nguyenhuuquang.hotelmanagement.entity.Promotion;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.entity.enums.PromotionType;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.PromotionRepository;
import com.nguyenhuuquang.hotelmanagement.service.PromotionService;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepo;
    private final SystemLogService logService;

    @Override
    @Transactional
    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        if (promotionRepo.findByCode(promotionDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại");
        }

        Promotion promotion = Promotion.builder()
                .code(promotionDTO.getCode().toUpperCase())
                .name(promotionDTO.getName())
                .description(promotionDTO.getDescription())
                .type(PromotionType.valueOf(promotionDTO.getType()))
                .value(promotionDTO.getValue())
                .maxDiscount(promotionDTO.getMaxDiscount())
                .minBookingAmount(promotionDTO.getMinBookingAmount())
                .startDate(promotionDTO.getStartDate())
                .endDate(promotionDTO.getEndDate())
                .maxUsage(promotionDTO.getMaxUsage())
                .active(true)
                .build();

        promotion = promotionRepo.save(promotion);

        logService.log(LogType.SUCCESS, "Thêm khuyến mãi", "Admin",
                String.format("Đã thêm mã khuyến mãi %s", promotion.getCode()));

        return convertToDTO(promotion);
    }

    @Override
    @Transactional
    public PromotionDTO updatePromotion(Long id, PromotionDTO promotionDTO) {
        Promotion promotion = promotionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi"));

        promotion.setName(promotionDTO.getName());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setType(PromotionType.valueOf(promotionDTO.getType()));
        promotion.setValue(promotionDTO.getValue());
        promotion.setMaxDiscount(promotionDTO.getMaxDiscount());
        promotion.setMinBookingAmount(promotionDTO.getMinBookingAmount());
        promotion.setStartDate(promotionDTO.getStartDate());
        promotion.setEndDate(promotionDTO.getEndDate());
        promotion.setMaxUsage(promotionDTO.getMaxUsage());
        promotion.setActive(promotionDTO.getActive());

        promotion = promotionRepo.save(promotion);

        logService.log(LogType.INFO, "Cập nhật khuyến mãi", "Admin",
                String.format("Đã cập nhật mã khuyến mãi %s", promotion.getCode()));

        return convertToDTO(promotion);
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi"));

        String code = promotion.getCode();
        promotionRepo.delete(promotion);

        logService.log(LogType.WARNING, "Xóa khuyến mãi", "Admin",
                String.format("Đã xóa mã khuyến mãi %s", code));
    }

    @Override
    public PromotionDTO getPromotionById(Long id) {
        Promotion promotion = promotionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi"));
        return convertToDTO(promotion);
    }

    @Override
    public PromotionDTO getPromotionByCode(String code) {
        Promotion promotion = promotionRepo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã khuyến mãi"));
        return convertToDTO(promotion);
    }

    @Override
    public List<PromotionDTO> getAllPromotions() {
        return promotionRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionDTO> getActivePromotions() {
        return promotionRepo.findActivePromotions(LocalDate.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionDTO> getPromotionsByType(String type) {
        PromotionType promotionType = PromotionType.valueOf(type.toUpperCase());
        return promotionRepo.findByType(promotionType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PromotionDTO togglePromotionStatus(Long id) {
        Promotion promotion = promotionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi"));

        promotion.setActive(!promotion.getActive());
        promotion = promotionRepo.save(promotion);

        logService.log(LogType.INFO, "Thay đổi trạng thái khuyến mãi", "Admin",
                String.format("Đã %s mã khuyến mãi %s",
                        promotion.getActive() ? "kích hoạt" : "vô hiệu hóa",
                        promotion.getCode()));

        return convertToDTO(promotion);
    }

    @Override
    public BigDecimal calculateDiscount(String promotionCode, BigDecimal bookingAmount) {
        Promotion promotion = promotionRepo.findValidPromotionByCode(
                promotionCode.toUpperCase(), LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không hợp lệ hoặc đã hết hạn"));

        if (promotion.getMinBookingAmount() != null &&
                bookingAmount.compareTo(promotion.getMinBookingAmount()) < 0) {
            throw new IllegalArgumentException(
                    String.format("Giá trị đơn hàng tối thiểu là %s", promotion.getMinBookingAmount()));
        }

        BigDecimal discount = BigDecimal.ZERO;

        switch (promotion.getType()) {
            case PERCENTAGE:
                discount = bookingAmount.multiply(promotion.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                if (promotion.getMaxDiscount() != null &&
                        discount.compareTo(promotion.getMaxDiscount()) > 0) {
                    discount = promotion.getMaxDiscount();
                }
                break;

            case FIXED_AMOUNT:
                discount = promotion.getValue();
                if (discount.compareTo(bookingAmount) > 0) {
                    discount = bookingAmount;
                }
                break;

            default:
                throw new IllegalArgumentException("Loại khuyến mãi không được hỗ trợ");
        }

        return discount;
    }

    @Override
    public PromotionDTO validatePromotionCode(String code, BigDecimal bookingAmount) {
        Promotion promotion = promotionRepo.findValidPromotionByCode(
                code.toUpperCase(), LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không hợp lệ hoặc đã hết hạn"));

        if (promotion.getMinBookingAmount() != null &&
                bookingAmount.compareTo(promotion.getMinBookingAmount()) < 0) {
            throw new IllegalArgumentException(
                    String.format("Giá trị đơn hàng tối thiểu là %s VNĐ", promotion.getMinBookingAmount()));
        }

        return convertToDTO(promotion);
    }

    private PromotionDTO convertToDTO(Promotion promotion) {
        return PromotionDTO.builder()
                .id(promotion.getId())
                .code(promotion.getCode())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .type(promotion.getType().name())
                .value(promotion.getValue())
                .maxDiscount(promotion.getMaxDiscount())
                .minBookingAmount(promotion.getMinBookingAmount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .maxUsage(promotion.getMaxUsage())
                .usedCount(promotion.getUsedCount())
                .active(promotion.getActive())
                .build();
    }
}