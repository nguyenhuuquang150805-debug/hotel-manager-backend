package com.nguyenhuuquang.hotelmanagement.service;

import com.nguyenhuuquang.hotelmanagement.dto.AuthResponse;
import com.nguyenhuuquang.hotelmanagement.dto.ChangePasswordRequest;
import com.nguyenhuuquang.hotelmanagement.dto.ForgotPasswordRequest;
import com.nguyenhuuquang.hotelmanagement.dto.LoginRequest;
import com.nguyenhuuquang.hotelmanagement.dto.RegisterRequest;
import com.nguyenhuuquang.hotelmanagement.dto.ResetPasswordRequest;
import com.nguyenhuuquang.hotelmanagement.entity.User;

public interface AuthService {
    User register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}