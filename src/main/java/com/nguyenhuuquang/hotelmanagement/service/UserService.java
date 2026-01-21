package com.nguyenhuuquang.hotelmanagement.service;

import com.nguyenhuuquang.hotelmanagement.dto.UpdateProfileRequest;
import com.nguyenhuuquang.hotelmanagement.entity.User;

public interface UserService {
    User getUserById(Long id);

    User updateProfile(Long id, UpdateProfileRequest request);
}