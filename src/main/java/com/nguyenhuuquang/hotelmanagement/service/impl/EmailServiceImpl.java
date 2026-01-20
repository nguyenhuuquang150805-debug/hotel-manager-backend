package com.nguyenhuuquang.hotelmanagement.service.impl;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            log.info("📧 ============= EMAIL SENDING START =============");
            log.info("📧 From: {}", fromEmail);
            log.info("📧 To: {}", to);
            log.info("🔑 OTP: {}", otp);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Mã OTP đặt lại mật khẩu - Hotel Management");
            message.setText(
                    "Xin chào,\n\n" +
                            "Mã OTP của bạn là: " + otp + "\n\n" +
                            "Mã này có hiệu lực trong 5 phút.\n\n" +
                            "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                            "Trân trọng,\n" +
                            "Hotel Management Team");

            log.info("📤 Attempting to send email via SMTP...");
            long startTime = System.currentTimeMillis();

            mailSender.send(message);

            long endTime = System.currentTimeMillis();
            log.info("✅ Email sent successfully in {}ms", (endTime - startTime));
            log.info("📧 ============= EMAIL SENDING END =============");

        } catch (Exception e) {
            log.error("❌ ============= EMAIL SENDING FAILED =============");
            log.error("❌ Recipient: {}", to);
            log.error("❌ Error type: {}", e.getClass().getName());
            log.error("❌ Error message: {}", e.getMessage());
            log.error("❌ Full stack trace:", e);
            log.error("❌ ============================================");
        }
    }
}