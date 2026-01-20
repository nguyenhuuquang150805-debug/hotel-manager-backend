package com.nguyenhuuquang.hotelmanagement.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nguyenhuuquang.hotelmanagement.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            log.info("Preparing to send OTP email to: {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@hotelmanagement.com"); // Thêm sender
            message.setTo(to);
            message.setSubject("Mã OTP đặt lại mật khẩu - Hotel Management");
            message.setText(
                    "Xin chào,\n\n" +
                            "Mã OTP của bạn là: " + otp + "\n\n" +
                            "Mã này có hiệu lực trong 5 phút.\n\n" +
                            "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                            "Trân trọng,\n" +
                            "Hotel Management Team");

            mailSender.send(message);
            log.info("✅ OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to: {}", to, e);

        }
    }
}