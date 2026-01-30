package com.nguyenhuuquang.hotelmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.nguyenhuuquang.hotelmanagement.entity.enums.BookingStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "nights", nullable = false)
    private Integer nights;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    @Column(name = "room_amount", nullable = false)
    private BigDecimal roomAmount;

    @Column(name = "service_amount")
    private BigDecimal serviceAmount;

    // ✨ Thêm các field mới cho promotion
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "deposit")
    private BigDecimal deposit;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "actual_check_in")
    private LocalDate actualCheckIn;

    @Column(name = "actual_check_out")
    private LocalDate actualCheckOut;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingService> bookingServices = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingPromotion> bookingPromotions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (deposit == null) {
            deposit = BigDecimal.ZERO;
        }
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (serviceAmount == null) {
            serviceAmount = BigDecimal.ZERO;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
        if (numberOfGuests == null) {
            numberOfGuests = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void recalculateTotalAmount() {
        BigDecimal calculatedServiceAmount = BigDecimal.ZERO;
        if (bookingServices != null && !bookingServices.isEmpty()) {
            calculatedServiceAmount = bookingServices.stream()
                    .map(BookingService::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        this.serviceAmount = calculatedServiceAmount;

        BigDecimal subtotal = this.roomAmount.add(this.serviceAmount);

        BigDecimal calculatedDiscountAmount = BigDecimal.ZERO;
        if (bookingPromotions != null && !bookingPromotions.isEmpty()) {
            calculatedDiscountAmount = bookingPromotions.stream()
                    .map(BookingPromotion::getDiscountAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        this.discountAmount = calculatedDiscountAmount;

        this.totalAmount = subtotal.subtract(this.discountAmount);

        if (this.totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    public void addBookingService(BookingService bs) {
        if (this.bookingServices == null)
            this.bookingServices = new ArrayList<>();
        this.bookingServices.add(bs);
        bs.setBooking(this);
    }

    public void removeBookingService(BookingService bookingService) {
        bookingServices.remove(bookingService);
        bookingService.setBooking(null);
        recalculateTotalAmount();
    }

    public void addBookingPromotion(BookingPromotion bp) {
        if (this.bookingPromotions == null)
            this.bookingPromotions = new ArrayList<>();
        this.bookingPromotions.add(bp);
        bp.setBooking(this);
    }

    public void removeBookingPromotion(BookingPromotion bookingPromotion) {
        bookingPromotions.remove(bookingPromotion);
        bookingPromotion.setBooking(null);
        recalculateTotalAmount();
    }
}