package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.BookingDTO;
import com.nguyenhuuquang.hotelmanagement.dto.CreateBookingRequest;
import com.nguyenhuuquang.hotelmanagement.dto.DashboardStatsDTO;
import com.nguyenhuuquang.hotelmanagement.entity.Booking;
import com.nguyenhuuquang.hotelmanagement.entity.Room;
import com.nguyenhuuquang.hotelmanagement.entity.enums.BookingStatus;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.entity.enums.RoomStatus;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.BookingRepository;
import com.nguyenhuuquang.hotelmanagement.repository.RoomRepository;
import com.nguyenhuuquang.hotelmanagement.service.BookingService;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final SystemLogService logService;

    @Override
    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request) {
        Room room = roomRepo.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng"));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Phòng không khả dụng");
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (nights <= 0) {
            throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng");
        }

        Booking booking = Booking.builder()
                .room(room)
                .customerName(request.getCustomerName())
                .phone(request.getPhone())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .nights((int) nights)
                .totalAmount(room.getPrice().multiply(java.math.BigDecimal.valueOf(nights)))
                .deposit(request.getDeposit())
                .status(BookingStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        booking = bookingRepo.save(booking);

        room.setStatus(RoomStatus.OCCUPIED);
        roomRepo.save(room);

        logService.log(LogType.SUCCESS, "Đặt phòng", "Admin",
                String.format("Đã đặt phòng %s cho khách %s", room.getRoomNumber(), request.getCustomerName()),
                String.format("Phòng %s, %d đêm, tổng tiền %s", room.getRoomNumber(), nights,
                        booking.getTotalAmount()));

        return convertToDTO(booking);
    }

    @Override
    @Transactional
    public void checkOut(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepo.save(booking);

        Room room = booking.getRoom();
        room.setStatus(RoomStatus.CLEANING);
        roomRepo.save(room);

        logService.log(LogType.SUCCESS, "Trả phòng", "Admin",
                String.format("Khách đã trả phòng %s", room.getRoomNumber()),
                "Thanh toán đầy đủ, phòng chuyển sang trạng thái dọn dẹp");
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);

        Room room = booking.getRoom();
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepo.save(room);

        logService.log(LogType.SUCCESS, "Hủy đặt phòng", "Admin",
                String.format("Đã hủy đặt phòng %s", room.getRoomNumber()),
                "Hoàn tiền cọc cho khách");
    }

    @Override
    public List<BookingDTO> getActiveBookings() {
        return bookingRepo.findByStatus(BookingStatus.ACTIVE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        return bookingRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));
        return convertToDTO(booking);
    }

    @Override
    public DashboardStatsDTO getBookingStats() {
        LocalDate today = LocalDate.now();
        Long todayRentals = bookingRepo.countTodayActiveBookings(today);
        Long occupiedRooms = roomRepo.countByStatus(RoomStatus.OCCUPIED);
        Long waitingRooms = roomRepo.countByStatus(RoomStatus.WAITING);
        Long cleaningRooms = roomRepo.countByStatus(RoomStatus.CLEANING);
        Long totalRooms = roomRepo.count();
        Long availableRooms = roomRepo.countByStatus(RoomStatus.AVAILABLE);

        return DashboardStatsDTO.builder()
                .todayRentals(todayRentals)
                .occupiedRooms(occupiedRooms)
                .waitingRooms(waitingRooms)
                .cleaningRooms(cleaningRooms)
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .build();
    }

    private BookingDTO convertToDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .roomId(booking.getRoom().getId())
                .roomNumber(booking.getRoom().getRoomNumber())
                .customerName(booking.getCustomerName())
                .phone(booking.getPhone())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .nights(booking.getNights())
                .totalAmount(booking.getTotalAmount())
                .deposit(booking.getDeposit())
                .status(booking.getStatus().name())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}