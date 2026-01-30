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
import com.nguyenhuuquang.hotelmanagement.entity.enums.BookingStatus;
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

    @PostMapping("/create-checkout")
    public ResponseEntity<?> createCheckoutPayment(@RequestBody CreateCheckoutPaymentDto dto) {
        try {
            System.out.println(
                    "📥 Tạo thanh toán checkout: bookingId=" + dto.bookingId + ", remainingAmount="
                            + dto.remainingAmount);

            // 1. Tìm booking
            Booking booking = bookingRepository.findById(dto.bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking #" + dto.bookingId));

            // 2. Validate
            if (booking.getStatus() != BookingStatus.CHECKED_IN) {
                throw new RuntimeException("Booking phải ở trạng thái CHECKED_IN để thanh toán checkout");
            }

            // 3. Tạo order code
            Long orderCode = System.currentTimeMillis() / 1000;

            // 4. Lưu order code vào notes
            String currentNotes = booking.getNotes() != null ? booking.getNotes() : "";
            booking.setNotes(currentNotes + "\n[PAYOS_CHECKOUT_ORDER_CODE:" + orderCode + "]");
            bookingRepository.save(booking);

            // 5. Tạo description (giới hạn 25 ký tự)
            String description = String.format("Checkout P%s", booking.getRoom().getRoomNumber());
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }

            // 6. Gọi PayOS API
            Map<String, Object> resp = payosService.createPaymentLink(
                    orderCode,
                    dto.remainingAmount,
                    description,
                    dto.returnUrl != null ? dto.returnUrl : "myapp://payment-return",
                    dto.cancelUrl != null ? dto.cancelUrl : "myapp://payment-return",
                    dto.expiredAt);

            System.out.println("✅ PayOS checkout response: " + resp);

            // 7. Validate response
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

            // 8. Trả response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentUrl", checkoutUrl);
            response.put("orderCode", orderCode);
            response.put("bookingId", dto.bookingId);
            response.put("amount", dto.remainingAmount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Lỗi tạo checkout payment: " + e.getMessage());
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

            // ✅ Tìm booking - kiểm tra cả 2 loại order code
            Booking booking = bookingRepository.findAll().stream()
                    .filter(b -> b.getNotes() != null &&
                            (b.getNotes().contains("[PAYOS_ORDER_CODE:" + orderCode + "]") ||
                                    b.getNotes().contains("[PAYOS_CHECKOUT_ORDER_CODE:" + orderCode + "]")))
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                System.err.println("❌ Không tìm thấy booking với orderCode: " + orderCode);
                return ResponseEntity.status(404).body(Map.of("error", "Booking not found"));
            }

            // ✅ Xác định loại thanh toán
            boolean isCheckout = booking.getNotes().contains("[PAYOS_CHECKOUT_ORDER_CODE:" + orderCode + "]");
            String paymentType = isCheckout ? "CHECKOUT" : "DEPOSIT";
            System.out.println("💳 Payment type: " + paymentType);

            if ("PAID".equals(status)) {
                BigDecimal paidAmount = BigDecimal.valueOf(amount);

                if (isCheckout) {
                    // ✅ Thanh toán checkout - Cộng vào paidAmount
                    booking.setPaidAmount(booking.getPaidAmount().add(paidAmount));

                    String paymentInfo = String.format(
                            "\n[CHECKOUT_PAYMENT_SUCCESS: %s | TxID: %s | Amount: %d | Time: %s]",
                            orderCode, transactionId, amount, LocalDateTime.now());
                    booking.setNotes(booking.getNotes() + paymentInfo);

                    System.out.println("✅ Đã cập nhật checkout payment: paidAmount=" +
                            booking.getPaidAmount() + " cho booking #" + booking.getId());
                } else {
                    // ✅ Thanh toán deposit - Cập nhật deposit và paidAmount
                    booking.setDeposit(paidAmount);
                    booking.setPaidAmount(booking.getPaidAmount().add(paidAmount));

                    String paymentInfo = String.format(
                            "\n[PAYMENT_SUCCESS: %s | TxID: %s | Amount: %d | Time: %s]",
                            orderCode, transactionId, amount, LocalDateTime.now());
                    booking.setNotes(booking.getNotes() + paymentInfo);

                    System.out.println("✅ Đã cập nhật deposit=" + paidAmount +
                            " và paidAmount cho booking #" + booking.getId());
                }

                bookingRepository.save(booking);

            } else if ("CANCELLED".equals(status)) {
                String cancelInfo = String.format("\n[%s_PAYMENT_CANCELLED: %s | Time: %s]",
                        paymentType, orderCode, LocalDateTime.now());
                booking.setNotes(booking.getNotes() + cancelInfo);
                bookingRepository.save(booking);

                System.out.println("❌ Thanh toán " + paymentType + " bị hủy cho booking #" + booking.getId());
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

            // 1. Gọi PayOS để check status
            Map<String, Object> payosStatus = payosService.getPaymentStatus(orderCode);

            // 2. Tìm booking - kiểm tra cả 2 loại
            Booking booking = bookingRepository.findAll().stream()
                    .filter(b -> b.getNotes() != null &&
                            (b.getNotes().contains("[PAYOS_ORDER_CODE:" + orderCode + "]") ||
                                    b.getNotes().contains("[PAYOS_CHECKOUT_ORDER_CODE:" + orderCode + "]")))
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", "Không tìm thấy booking"));
            }

            // 3. Xác định loại thanh toán
            boolean isCheckout = booking.getNotes().contains("[PAYOS_CHECKOUT_ORDER_CODE:" + orderCode + "]");
            String paymentType = isCheckout ? "CHECKOUT" : "DEPOSIT";

            boolean isPaid = false;
            if (payosStatus != null && "00".equals(payosStatus.get("code"))) {
                Map<String, Object> data = (Map<String, Object>) payosStatus.get("data");
                String status = (String) data.get("status");

                if ("PAID".equals(status)) {
                    isPaid = true;

                    // Check nếu chưa cập nhật vào database
                    String successMarker = isCheckout ? "[CHECKOUT_PAYMENT_SUCCESS: " + orderCode
                            : "[PAYMENT_SUCCESS: " + orderCode;

                    if (!booking.getNotes().contains(successMarker)) {
                        Long amount = Long.parseLong(data.get("amount").toString());
                        BigDecimal paidAmount = BigDecimal.valueOf(amount);

                        if (isCheckout) {
                            // Checkout: chỉ cộng vào paidAmount
                            booking.setPaidAmount(booking.getPaidAmount().add(paidAmount));
                        } else {
                            // Deposit: cập nhật cả deposit và paidAmount
                            booking.setDeposit(paidAmount);
                            booking.setPaidAmount(booking.getPaidAmount().add(paidAmount));
                        }

                        String transactionId = data.get("id") != null ? data.get("id").toString() : "N/A";
                        String paymentInfo = String.format(
                                "\n[%s_PAYMENT_SUCCESS: %s | TxID: %s | Amount: %d | Time: %s]",
                                paymentType, orderCode, transactionId, amount, LocalDateTime.now());
                        booking.setNotes(booking.getNotes() + paymentInfo);

                        bookingRepository.save(booking);
                        System.out.println("✅ Đã cập nhật " + paymentType + " payment cho booking #" + booking.getId());
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bookingId", booking.getId(),
                    "orderCode", orderCode,
                    "paymentType", paymentType,
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

    public static class CreateCheckoutPaymentDto {
        public Long bookingId;
        public Long remainingAmount;
        public String returnUrl;
        public String cancelUrl;
        public Integer expiredAt;
    }

}