package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.nguyenhuuquang.hotelmanagement.config.JwtUtil;
import com.nguyenhuuquang.hotelmanagement.dto.AuthResponse;
import com.nguyenhuuquang.hotelmanagement.dto.LoginRequest;
import com.nguyenhuuquang.hotelmanagement.dto.RegisterRequest;
import com.nguyenhuuquang.hotelmanagement.entity.User;
import com.nguyenhuuquang.hotelmanagement.exception.AuthenticationException;
import com.nguyenhuuquang.hotelmanagement.repository.UserRepository;
import com.nguyenhuuquang.hotelmanagement.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
}