package com.nguyenhuuquang.hotelmanagement.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nguyenhuuquang.hotelmanagement.exception.AuthenticationException;
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

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 2000;

    @Override
    @Async
    public void sendOtpEmail(String to, String otp) {
        log.info("📧 ============= EMAIL SENDING START =============");
        log.info("📧 From: {}", fromEmail);
        log.info("📧 To: {}", to);
        log.info("🔑 OTP: {}", otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Mã OTP đặt lại mật khẩu - Hotel Management");
        message.setText(buildEmailContent(otp));

        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                log.info("📤 Attempt {}/{} - Sending email via SMTP...", attempt, MAX_RETRY);
                long startTime = System.currentTimeMillis();

                mailSender.send(message);

                long duration = System.currentTimeMillis() - startTime;
                log.info("✅ Email sent successfully in {}ms on attempt {}", duration, attempt);
                log.info("📧 ============= EMAIL SENDING END =============");
                return;

            } catch (MailException e) {
                lastException = e;
                log.error("❌ Attempt {}/{} failed", attempt, MAX_RETRY);
                log.error("❌ Error type: {}", e.getClass().getSimpleName());
                log.error("❌ Error message: {}", e.getMessage());

                if (e.getCause() != null) {
                    log.error("❌ Root cause: {}", e.getCause().getMessage());
                }

                if (attempt < MAX_RETRY) {
                    try {
                        log.info("⏳ Waiting {}ms before retry...", RETRY_DELAY_MS);
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("❌ Retry interrupted");
                        break;
                    }
                }
            }
        }

        // Tất cả attempts đều fail
        log.error("❌ ============= ALL ATTEMPTS FAILED =============");
        log.error("❌ Recipient: {}", to);
        log.error("❌ Final error:", lastException);
        log.error("❌ ============================================");

        // Throw exception để AuthService catch được
        throw new AuthenticationException("Không thể gửi email. Vui lòng thử lại sau.");
    }

    private String buildEmailContent(String otp) {
        return String.format(
                "Xin chào,\n\n" +
                        "Mã OTP của bạn là: %s\n\n" +
                        "Mã này có hiệu lực trong 5 phút.\n\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\n" +
                        "Hotel Management Team",
                otp);
    }
}