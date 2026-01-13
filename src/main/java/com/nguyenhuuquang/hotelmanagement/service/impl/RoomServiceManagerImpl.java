package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.BookingServiceDTO;
import com.nguyenhuuquang.hotelmanagement.entity.Booking;
import com.nguyenhuuquang.hotelmanagement.entity.BookingService;
import com.nguyenhuuquang.hotelmanagement.entity.User;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.BookingRepository;
import com.nguyenhuuquang.hotelmanagement.repository.BookingServiceRepository;
import com.nguyenhuuquang.hotelmanagement.repository.ServiceRepository;
import com.nguyenhuuquang.hotelmanagement.repository.UserRepository;
import com.nguyenhuuquang.hotelmanagement.service.RoomServiceManager;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceManagerImpl implements RoomServiceManager {

    private final BookingServiceRepository bookingServiceRepo;
    private final BookingRepository bookingRepo;
    private final ServiceRepository serviceRepo;
    private final UserRepository userRepo;
    private final SystemLogService logService;

    @Override
    public List<BookingServiceDTO> getServicesByBooking(Long bookingId) {
        return bookingServiceRepo.findByBookingId(bookingId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingServiceDTO addServiceToBooking(Long bookingId, Long serviceId,
            Integer quantity, String notes,
            String username) {
        // Validate booking
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

        // Validate service
        com.nguyenhuuquang.hotelmanagement.entity.Service service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));

        if (!service.getAvailable()) {
            throw new IllegalStateException("Dịch vụ hiện không khả dụng");
        }

        // Validate user
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // Validate quantity
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        // Create booking service
        BookingService bookingService = BookingService.builder()
                .booking(booking)
                .service(service)
                .quantity(quantity)
                .unitPrice(service.getPrice())
                .totalPrice(service.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .serviceDate(LocalDateTime.now())
                .notes(notes)
                .createdBy(user)
                .build();

        bookingService = bookingServiceRepo.save(bookingService);

        // Update booking total amount
        booking.recalculateTotalAmount();
        bookingRepo.save(booking);

        // Log action
        logService.log(LogType.SUCCESS, "Thêm dịch vụ", username,
                String.format("Đã thêm dịch vụ '%s' (x%d) vào booking #%d",
                        service.getName(), quantity, bookingId),
                String.format("Tổng tiền dịch vụ: %s, Tổng tiền booking: %s",
                        bookingService.getTotalPrice(), booking.getTotalAmount()));

        return convertToDTO(bookingService);
    }

    @Override
    public BookingServiceDTO updateServiceQuantity(Long bookingServiceId,
            Integer quantity, String notes) {
        BookingService bookingService = bookingServiceRepo.findById(bookingServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ trong booking"));

        BigDecimal oldTotal = bookingService.getTotalPrice();

        // Update quantity if provided
        if (quantity != null) {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }
            bookingService.setQuantity(quantity);
            bookingService.setTotalPrice(
                    bookingService.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        // Update notes if provided
        if (notes != null) {
            bookingService.setNotes(notes);
        }

        bookingService = bookingServiceRepo.save(bookingService);

        // Update booking total amount
        Booking booking = bookingService.getBooking();
        booking.recalculateTotalAmount();
        bookingRepo.save(booking);

        // Log action
        logService.log(LogType.INFO, "Cập nhật dịch vụ", "Admin",
                String.format("Đã cập nhật dịch vụ '%s' trong booking #%d",
                        bookingService.getService().getName(), booking.getId()),
                String.format("Số lượng: %d, Tiền cũ: %s, Tiền mới: %s",
                        quantity, oldTotal, bookingService.getTotalPrice()));

        return convertToDTO(bookingService);
    }

    @Override
    public void removeServiceFromBooking(Long bookingServiceId) {
        BookingService bookingService = bookingServiceRepo.findById(bookingServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ trong booking"));

        Booking booking = bookingService.getBooking();
        String serviceName = bookingService.getService().getName();
        BigDecimal removedAmount = bookingService.getTotalPrice();

        // Delete booking service
        bookingServiceRepo.delete(bookingService);

        // Update booking total amount
        booking.recalculateTotalAmount();
        bookingRepo.save(booking);

        // Log action
        logService.log(LogType.WARNING, "Xóa dịch vụ", "Admin",
                String.format("Đã xóa dịch vụ '%s' khỏi booking #%d",
                        serviceName, booking.getId()),
                String.format("Số tiền giảm: %s, Tổng tiền booking còn lại: %s",
                        removedAmount, booking.getTotalAmount()));
    }

    @Override
    public BigDecimal calculateTotalServiceAmount(Long bookingId) {
        return bookingServiceRepo.calculateTotalServiceAmount(bookingId);
    }

    @Override
    public BookingServiceDTO getBookingServiceById(Long id) {
        BookingService bookingService = bookingServiceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ trong booking"));
        return convertToDTO(bookingService);
    }

    @Override
    public List<Object[]> getMostUsedServices() {
        return bookingServiceRepo.findMostUsedServices();
    }

    private BookingServiceDTO convertToDTO(BookingService bookingService) {
        return BookingServiceDTO.builder()
                .id(bookingService.getId())
                .bookingId(bookingService.getBooking().getId())
                .serviceId(bookingService.getService().getId())
                .serviceName(bookingService.getService().getName())
                .serviceCategory(bookingService.getService().getCategory())
                .quantity(bookingService.getQuantity())
                .unitPrice(bookingService.getUnitPrice())
                .totalPrice(bookingService.getTotalPrice())
                .serviceDate(bookingService.getServiceDate())
                .notes(bookingService.getNotes())
                .createdByName(
                        bookingService.getCreatedBy() != null ? bookingService.getCreatedBy().getFullName() : null)
                .createdAt(bookingService.getCreatedAt())
                .updatedAt(bookingService.getUpdatedAt())
                .build();
    }
}