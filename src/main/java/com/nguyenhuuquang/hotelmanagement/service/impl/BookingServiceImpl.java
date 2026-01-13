package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

                // Tính toán các giá trị
                BigDecimal roomAmount = room.getPrice().multiply(BigDecimal.valueOf(nights));
                BigDecimal serviceAmount = BigDecimal.ZERO;
                BigDecimal totalAmount = roomAmount.add(serviceAmount);
                BigDecimal deposit = request.getDeposit() != null ? request.getDeposit() : BigDecimal.ZERO;

                Booking booking = Booking.builder()
                                .room(room)
                                .customerName(request.getCustomerName())
                                .phone(request.getPhone())
                                .checkIn(request.getCheckIn())
                                .checkOut(request.getCheckOut())
                                .nights((int) nights)
                                .numberOfGuests(1) // Default value
                                .roomAmount(roomAmount)
                                .serviceAmount(serviceAmount)
                                .totalAmount(totalAmount)
                                .deposit(deposit)
                                .paidAmount(BigDecimal.ZERO)
                                .status(BookingStatus.PENDING)
                                .notes(request.getNotes())
                                .createdBy(1L)
                                .build();

                booking = bookingRepo.save(booking);

                room.setStatus(RoomStatus.RESERVED);
                roomRepo.save(room);

                logService.log(LogType.SUCCESS, "Tạo đặt phòng", "Admin",
                                String.format("Đã tạo đặt phòng %s cho khách %s", room.getRoomNumber(),
                                                request.getCustomerName()),
                                String.format("Phòng %s, %d đêm, tiền phòng %s, tổng tiền %s, trạng thái: Chờ xác nhận",
                                                room.getRoomNumber(), nights, roomAmount, totalAmount));

                return convertToDTO(booking);
        }

        @Override
        @Transactional
        public BookingDTO confirmBooking(Long bookingId) {
                Booking booking = bookingRepo.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

                if (booking.getStatus() != BookingStatus.PENDING) {
                        throw new IllegalStateException("Chỉ có thể xác nhận đặt phòng ở trạng thái chờ xác nhận");
                }

                booking.setStatus(BookingStatus.CONFIRMED);
                booking = bookingRepo.save(booking);

                logService.log(LogType.SUCCESS, "Xác nhận đặt phòng", "Admin",
                                String.format("Đã xác nhận đặt phòng %s cho khách %s",
                                                booking.getRoom().getRoomNumber(), booking.getCustomerName()),
                                "Đặt phòng đã được xác nhận, chờ khách nhận phòng");

                return convertToDTO(booking);
        }

        @Override
        @Transactional
        public BookingDTO checkIn(Long bookingId) {
                Booking booking = bookingRepo.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

                if (booking.getStatus() != BookingStatus.CONFIRMED) {
                        throw new IllegalStateException("Chỉ có thể nhận phòng khi đặt phòng đã được xác nhận");
                }

                booking.setStatus(BookingStatus.CHECKED_IN);
                booking.setActualCheckIn(LocalDate.now());
                booking = bookingRepo.save(booking);

                Room room = booking.getRoom();
                room.setStatus(RoomStatus.OCCUPIED);
                roomRepo.save(room);

                logService.log(LogType.SUCCESS, "Nhận phòng", "Admin",
                                String.format("Khách %s đã nhận phòng %s", booking.getCustomerName(),
                                                room.getRoomNumber()),
                                "Check-in thành công, phòng đang được sử dụng");

                return convertToDTO(booking);
        }

        @Override
        @Transactional
        public BookingDTO checkOut(Long bookingId) {
                Booking booking = bookingRepo.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

                if (booking.getStatus() != BookingStatus.CHECKED_IN) {
                        throw new IllegalStateException("Chỉ có thể trả phòng khi khách đã nhận phòng");
                }

                booking.setStatus(BookingStatus.CHECKED_OUT);
                booking.setActualCheckOut(LocalDate.now());
                booking = bookingRepo.save(booking);

                Room room = booking.getRoom();
                room.setStatus(RoomStatus.CLEANING);
                roomRepo.save(room);

                logService.log(LogType.SUCCESS, "Trả phòng", "Admin",
                                String.format("Khách %s đã trả phòng %s", booking.getCustomerName(),
                                                room.getRoomNumber()),
                                "Check-out thành công, phòng chuyển sang trạng thái dọn dẹp");

                return convertToDTO(booking);
        }

        @Override
        @Transactional
        public BookingDTO completeBooking(Long bookingId) {
                Booking booking = bookingRepo.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

                if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
                        throw new IllegalStateException("Chỉ có thể hoàn thành khi khách đã trả phòng");
                }

                booking.setStatus(BookingStatus.COMPLETED);
                booking = bookingRepo.save(booking);

                Room room = booking.getRoom();
                if (room.getStatus() == RoomStatus.CLEANING) {
                        room.setStatus(RoomStatus.AVAILABLE);
                        roomRepo.save(room);
                }

                logService.log(LogType.SUCCESS, "Hoàn thành đặt phòng", "Admin",
                                String.format("Đã hoàn thành đặt phòng %s", room.getRoomNumber()),
                                "Thanh toán đầy đủ, phòng sẵn sàng cho khách tiếp theo");

                return convertToDTO(booking);
        }

        @Override
        @Transactional
        public void cancelBooking(Long bookingId) {
                Booking booking = bookingRepo.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

                if (booking.getStatus() == BookingStatus.COMPLETED) {
                        throw new IllegalStateException("Không thể hủy đặt phòng đã hoàn thành");
                }

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                        throw new IllegalStateException("Đặt phòng đã bị hủy trước đó");
                }

                BookingStatus oldStatus = booking.getStatus();
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setCancelledAt(LocalDateTime.now());
                bookingRepo.save(booking);

                Room room = booking.getRoom();
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepo.save(room);

                logService.log(LogType.SUCCESS, "Hủy đặt phòng", "Admin",
                                String.format("Đã hủy đặt phòng %s (Trạng thái cũ: %s)", room.getRoomNumber(),
                                                oldStatus),
                                "Hoàn tiền cọc cho khách, phòng trở về trạng thái sẵn sàng");
        }

        @Override
        @Transactional
        public void markNoShow(Long bookingId) {
                Booking booking = bookingRepo.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng"));

                if (booking.getStatus() != BookingStatus.CONFIRMED) {
                        throw new IllegalStateException("Chỉ có thể đánh dấu No-Show cho đặt phòng đã xác nhận");
                }

                booking.setStatus(BookingStatus.NO_SHOW);
                booking.setCancelledAt(LocalDateTime.now());
                bookingRepo.save(booking);

                Room room = booking.getRoom();
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepo.save(room);

                logService.log(LogType.WARNING, "Khách không đến", "Admin",
                                String.format("Khách %s không đến nhận phòng %s", booking.getCustomerName(),
                                                room.getRoomNumber()),
                                "Không hoàn tiền cọc, phòng trở về trạng thái sẵn sàng");
        }

        @Override
        public List<BookingDTO> getActiveBookings() {
                List<Booking> pendingBookings = bookingRepo.findByStatus(BookingStatus.PENDING);
                List<Booking> confirmedBookings = bookingRepo.findByStatus(BookingStatus.CONFIRMED);
                List<Booking> checkedInBookings = bookingRepo.findByStatus(BookingStatus.CHECKED_IN);

                List<Booking> allActiveBookings = new ArrayList<>();
                allActiveBookings.addAll(pendingBookings);
                allActiveBookings.addAll(confirmedBookings);
                allActiveBookings.addAll(checkedInBookings);

                return allActiveBookings.stream()
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
        public List<BookingDTO> getBookingsByStatus(String status) {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                return bookingRepo.findByStatus(bookingStatus)
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
                Long reservedRooms = roomRepo.countByStatus(RoomStatus.RESERVED);
                Long cleaningRooms = roomRepo.countByStatus(RoomStatus.CLEANING);
                Long totalRooms = roomRepo.count();
                Long availableRooms = roomRepo.countByStatus(RoomStatus.AVAILABLE);

                return DashboardStatsDTO.builder()
                                .todayRentals(todayRentals)
                                .occupiedRooms(occupiedRooms)
                                .waitingRooms(reservedRooms)
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