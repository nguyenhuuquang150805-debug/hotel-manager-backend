package com.nguyenhuuquang.hotelmanagement.service;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
}