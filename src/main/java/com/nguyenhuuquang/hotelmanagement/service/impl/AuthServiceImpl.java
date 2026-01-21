package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.nguyenhuuquang.hotelmanagement.config.JwtUtil;
import com.nguyenhuuquang.hotelmanagement.dto.AuthResponse;
import com.nguyenhuuquang.hotelmanagement.dto.ChangePasswordRequest;
import com.nguyenhuuquang.hotelmanagement.dto.ForgotPasswordRequest;
import com.nguyenhuuquang.hotelmanagement.dto.LoginRequest;
import com.nguyenhuuquang.hotelmanagement.dto.RegisterRequest;
import com.nguyenhuuquang.hotelmanagement.dto.ResetPasswordRequest;
import com.nguyenhuuquang.hotelmanagement.entity.User;
import com.nguyenhuuquang.hotelmanagement.exception.AuthenticationException;
import com.nguyenhuuquang.hotelmanagement.repository.UserRepository;
import com.nguyenhuuquang.hotelmanagement.service.AuthService;
import com.nguyenhuuquang.hotelmanagement.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Override
    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.info("Registering user with email: {}", request.getEmail());
        log.debug("Encoded password length: {}", encodedPassword.length());

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role("USER")
                .status(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found: {}", request.getEmail());
                    return new AuthenticationException("Invalid email or password");
                });

        log.debug("User found, checking password...");
        log.debug("Stored password starts with: {}", user.getPassword().substring(0, 10));

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.debug("Password matches: {}", passwordMatches);

        if (!passwordMatches) {
            log.error("Password mismatch for user: {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        log.info("Login successful for user: {}", request.getEmail());
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        log.info("Change password request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found: {}", request.getEmail());
                    return new AuthenticationException("Không tìm thấy người dùng");
                });

        boolean oldPasswordMatches = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        if (!oldPasswordMatches) {
            log.error("Old password mismatch for user: {}", request.getEmail());
            throw new AuthenticationException("Mật khẩu cũ không chính xác");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", request.getEmail());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found: {}", request.getEmail());
                    return new AuthenticationException("Email không tồn tại trong hệ thống");
                });

        String resetToken = String.format("%06d", (int) (Math.random() * 1000000));
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Log OTP ra console
        log.info("========================================");
        log.info("🔑 OTP CODE FOR {}: {}", user.getEmail(), resetToken);
        log.info("========================================");

        try {
            emailService.sendResetPasswordEmail(user.getEmail(), resetToken);
            log.info("Email sending triggered for: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Error triggering email send: {}", e.getMessage());
            // Không throw exception - vẫn cho phép user lấy OTP từ logs
        }

        log.info("Reset token generated and saved for email: {}", request.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request with token");

        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> {
                    log.error("Invalid reset token: {}", request.getToken());
                    return new AuthenticationException("Mã xác thực không hợp lệ");
                });

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            log.error("Reset token expired for user: {}", user.getEmail());
            throw new AuthenticationException("Mã xác thực đã hết hạn");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

}