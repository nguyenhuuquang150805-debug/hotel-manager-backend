package com.nguyenhuuquang.hotelmanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nguyenhuuquang.hotelmanagement.entity.Booking;
import com.nguyenhuuquang.hotelmanagement.entity.Room;
import com.nguyenhuuquang.hotelmanagement.entity.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByRoom(Room room);

    List<Booking> findByCustomerName(String customerName);

    List<Booking> findByPhone(String phone);

    @Query("SELECT b FROM Booking b WHERE b.checkIn = :date AND b.status IN ('CONFIRMED', 'CHECKED_IN')")
    List<Booking> findByCheckInDateAndActiveStatus(@Param("date") LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkIn = :date AND b.status = 'CHECKED_IN'")
    Long countTodayActiveBookings(@Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.checkOut BETWEEN :startDate AND :endDate")
    List<Booking> findByCheckOutBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CHECKED_IN'")
    Long countCheckedInBookings();
}