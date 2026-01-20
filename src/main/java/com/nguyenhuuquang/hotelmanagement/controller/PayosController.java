package com.nguyenhuuquang.hotelmanagement.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenhuuquang.hotelmanagement.entity.Booking;
import com.nguyenhuuquang.hotelmanagement.repository.BookingRepository;
import com.nguyenhuuquang.hotelmanagement.service.PayosService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PayosController {

    private final PayosService payosService;
    private final BookingRepository bookingRepository;

    @PostMapping("/create-deposit")
    public ResponseEntity<?> createDepositPayment(@RequestBody CreateDepositPaymentDto dto) {
        try {
            System.out.println(
                    "📥 Tạo thanh toán cọc: bookingId=" + dto.bookingId + ", depositAmount=" + dto.depositAmount);

            Booking booking = bookingRepository.findById(dto.bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking #" + dto.bookingId));

            Long orderCode = System.currentTimeMillis() / 1000;

            String currentNotes = booking.getNotes() != null ? booking.getNotes() : "";
            booking.setNotes(currentNotes + "\n[PAYOS_ORDER_CODE:" + orderCode + "]");
            bookingRepository.save(booking);

            String description = String.format("Coc P%s", booking.getRoom().getRoomNumber());

            if (description.length() > 25) {
                description = description.substring(0, 25);
            }

            Map<String, Object> resp = payosService.createPaymentLink(
                    orderCode,
                    dto.depositAmount,
                    description,
                    dto.returnUrl != null ? dto.returnUrl : "myapp://payment-return",
                    dto.cancelUrl != null ? dto.cancelUrl : "myapp://payment-return",
                    dto.expiredAt);

            System.out.println("✅ PayOS full response: " + resp);

            if (resp == null) {
                throw new RuntimeException("PayOS trả về response null");
            }

            String code = (String) resp.get("code");
            if (!"00".equals(code)) {
                String errorMsg = (String) resp.get("desc");
                throw new RuntimeException("PayOS error: " + errorMsg);
            }

            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null) {
                throw new RuntimeException("PayOS response không có trường data");
            }

            String checkoutUrl = (String) data.get("checkoutUrl");
            if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                throw new RuntimeException("PayOS không trả về checkoutUrl");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentUrl", checkoutUrl);
            response.put("orderCode", orderCode);
            response.put("bookingId", dto.bookingId);
            response.put("amount", dto.depositAmount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> webhookData) {
        try {
            System.out.println("🔔 Nhận webhook từ PayOS: " + webhookData);

            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            Long orderCode = Long.parseLong(data.get("orderCode").toString());
            String status = (String) data.get("status");
            String transactionId = data.get("id").toString();
            Long amount = Long.parseLong(data.get("amount").toString());

            System.out.println("📋 Webhook: orderCode=" + orderCode + ", status=" + status);

            Booking booking = bookingRepository.findAll().stream()
                    .filter(b -> b.getNotes() != null && b.getNotes().contains("[PAYOS_ORDER_CODE:" + orderCode + "]"))
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                System.err.println("❌ Không tìm thấy booking với orderCode: " + orderCode);
                return ResponseEntity.status(404).body(Map.of("error", "Booking not found"));
            }

            if ("PAID".equals(status)) {
                BigDecimal paidAmount = BigDecimal.valueOf(amount);

                booking.setDeposit(paidAmount);
                booking.setPaidAmount(booking.getPaidAmount().add(paidAmount));

                String paymentInfo = String.format(
                        "\n[PAYMENT_SUCCESS: %s | TxID: %s | Amount: %d | Time: %s]",
                        orderCode, transactionId, amount, LocalDateTime.now());
                booking.setNotes(booking.getNotes() + paymentInfo);

                bookingRepository.save(booking);

                System.out.println(
                        "✅ Đã cập nhật deposit=" + paidAmount + " và paidAmount cho booking #" + booking.getId());
            } else if ("CANCELLED".equals(status)) {
                String cancelInfo = String.format("\n[PAYMENT_CANCELLED: %s | Time: %s]",
                        orderCode, LocalDateTime.now());
                booking.setNotes(booking.getNotes() + cancelInfo);
                bookingRepository.save(booking);

                System.out.println("❌ Thanh toán bị hủy cho booking #" + booking.getId());
            }

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            System.err.println("❌ Lỗi xử lý webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify/{orderCode}")
    public ResponseEntity<?> verifyPayment(@PathVariable Long orderCode) {
        try {
            System.out.println("🔍 Verify payment: " + orderCode);

            Map<String, Object> payosStatus = payosService.getPaymentStatus(orderCode);

            Booking booking = bookingRepository.findAll().stream()
                    .filter(b -> b.getNotes() != null && b.getNotes().contains("[PAYOS_ORDER_CODE:" + orderCode + "]"))
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", "Không tìm thấy booking"));
            }

            boolean isPaid = false;
            if (payosStatus != null && "00".equals(payosStatus.get("code"))) {
                Map<String, Object> data = (Map<String, Object>) payosStatus.get("data");
                String status = (String) data.get("status");

                if ("PAID".equals(status)) {
                    isPaid = true;

                    if (!booking.getNotes().contains("[PAYMENT_SUCCESS: " + orderCode)) {
                        Long amount = Long.parseLong(data.get("amount").toString());
                        BigDecimal paidAmount = BigDecimal.valueOf(amount);

                        booking.setDeposit(paidAmount);
                        booking.setPaidAmount(booking.getPaidAmount().add(paidAmount));

                        String transactionId = data.get("id") != null ? data.get("id").toString() : "N/A";
                        String paymentInfo = String.format(
                                "\n[PAYMENT_SUCCESS: %s | TxID: %s | Amount: %d | Time: %s]",
                                orderCode, transactionId, amount, LocalDateTime.now());
                        booking.setNotes(booking.getNotes() + paymentInfo);

                        bookingRepository.save(booking);
                        System.out.println("✅ Đã cập nhật deposit=" + paidAmount + " và paidAmount cho booking #"
                                + booking.getId());
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bookingId", booking.getId(),
                    "orderCode", orderCode,
                    "isPaid", isPaid,
                    "paidAmount", booking.getPaidAmount(),
                    "totalAmount", booking.getTotalAmount(),
                    "deposit", booking.getDeposit()));

        } catch (Exception e) {
            System.err.println("❌ Error verifying payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @GetMapping("/history/{bookingId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bookingId", bookingId,
                    "notes", booking.getNotes() != null ? booking.getNotes() : "",
                    "paidAmount", booking.getPaidAmount(),
                    "totalAmount", booking.getTotalAmount(),
                    "deposit", booking.getDeposit()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    public static class CreateDepositPaymentDto {
        public Long bookingId;
        public Long depositAmount;
        public String returnUrl;
        public String cancelUrl;
        public Integer expiredAt;
    }

}