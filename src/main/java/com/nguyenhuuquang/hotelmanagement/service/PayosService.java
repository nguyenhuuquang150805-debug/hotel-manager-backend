package com.nguyenhuuquang.hotelmanagement.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nguyenhuuquang.hotelmanagement.utils.HmacUtil;

@Service
public class PayosService {

    private final RestTemplate rest = new RestTemplate();

    @Value("${payos.clientId}")
    private String clientId;

    @Value("${payos.apiKey}")
    private String apiKey;

    @Value("${payos.checksumKey}")
    private String checksumKey;

    public Map<String, Object> createPaymentLink(Long orderCode, Long amount,
            String description, String returnUrl, String cancelUrl, Integer expiredAt) {

        System.out.println("🔐 Tạo signature PayOS...");
        System.out.println("   - Client ID: " + clientId);
        System.out.println("   - Order Code: " + orderCode);
        System.out.println("   - Amount: " + amount);

        // Signature data
        Map<String, String> sigParams = new HashMap<>();
        sigParams.put("amount", amount.toString());
        sigParams.put("cancelUrl", cancelUrl);
        sigParams.put("description", description);
        sigParams.put("orderCode", orderCode.toString());
        sigParams.put("returnUrl", returnUrl);

        String signatureData = HmacUtil.buildSignatureString(sigParams);
        String signature = HmacUtil.hmacSha256(checksumKey, signatureData);

        System.out.println("   - Signature: " + signature);

        // Body
        Map<String, Object> body = new HashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", amount);
        body.put("description", description);
        body.put("returnUrl", returnUrl);
        body.put("cancelUrl", cancelUrl);
        body.put("signature", signature);
        if (expiredAt != null)
            body.put("expiredAt", expiredAt);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        System.out.println("🌐 Gọi PayOS API...");

        ResponseEntity<Map> res = rest.postForEntity(
                "https://api-merchant.payos.vn/v2/payment-requests",
                entity,
                Map.class);

        System.out.println("✅ PayOS Response: " + res.getBody());

        return res.getBody();
    }
}