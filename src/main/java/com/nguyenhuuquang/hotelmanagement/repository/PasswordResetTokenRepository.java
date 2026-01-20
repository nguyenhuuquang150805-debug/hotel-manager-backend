package com.nguyenhuuquang.hotelmanagement.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nguyenhuuquang.hotelmanagement.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByEmailAndOtpAndUsedFalseAndExpiryTimeAfter(
            String email, String otp, LocalDateTime currentTime);

    void deleteByEmail(String email);
}