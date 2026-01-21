package com.nguyenhuuquang.hotelmanagement.service;

public interface EmailService {
    void sendResetPasswordEmail(String toEmail, String resetToken);
}