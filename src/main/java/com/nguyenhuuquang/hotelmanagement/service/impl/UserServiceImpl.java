package com.nguyenhuuquang.hotelmanagement.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.UpdateProfileRequest;
import com.nguyenhuuquang.hotelmanagement.entity.User;
import com.nguyenhuuquang.hotelmanagement.exception.NotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.UserRepository;
import com.nguyenhuuquang.hotelmanagement.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new NotFoundException("Không tìm thấy người dùng");
                });
    }

    @Override
    @Transactional
    public User updateProfile(Long id, UpdateProfileRequest request) {
        log.info("Updating profile for user id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new NotFoundException("Không tìm thấy người dùng");
                });

        // Cập nhật thông tin
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", user.getEmail());

        return updatedUser;
    }
}