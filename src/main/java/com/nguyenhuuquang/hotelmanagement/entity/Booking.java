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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "id_number", length = 20)
    private String idNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "actual_check_in")
    private LocalDateTime actualCheckIn;

    @Column(name = "actual_check_out")
    private LocalDateTime actualCheckOut;

    @Column(nullable = false)
    private Integer nights;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    @Column(name = "room_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal roomAmount;

    @Column(name = "service_amount", precision = 10, scale = 2)
    private BigDecimal serviceAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal deposit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Quan hệ với các dịch vụ được sử dụng
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingService> bookingServices = new ArrayList<>();

    // Quan hệ với các khoản thanh toán
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Khởi tạo các giá trị mặc định
        if (serviceAmount == null) {
            serviceAmount = BigDecimal.ZERO;
        }
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (deposit == null) {
            deposit = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addBookingService(BookingService bookingService) {
        bookingServices.add(bookingService);
        bookingService.setBooking(this);
        recalculateTotalAmount();
    }

    public void removeBookingService(BookingService bookingService) {
        bookingServices.remove(bookingService);
        bookingService.setBooking(null);
        recalculateTotalAmount();
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setBooking(this);
        recalculatePaidAmount();
    }

    public void recalculateTotalAmount() {
        serviceAmount = bookingServices.stream()
                .map(BookingService::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalAmount = roomAmount.add(serviceAmount);
    }

    public void recalculatePaidAmount() {
        paidAmount = payments.stream()
                .filter(p -> p.getStatus() == com.nguyenhuuquang.hotelmanagement.entity.enums.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount);
    }

    public boolean isFullyPaid() {
        return paidAmount.compareTo(totalAmount) >= 0;
    }
}