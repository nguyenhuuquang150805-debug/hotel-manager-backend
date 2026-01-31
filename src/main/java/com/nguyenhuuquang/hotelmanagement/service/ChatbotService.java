package com.nguyenhuuquang.hotelmanagement.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.nguyenhuuquang.hotelmanagement.dto.ChatRequest;
import com.nguyenhuuquang.hotelmanagement.dto.ChatResponse;
import com.nguyenhuuquang.hotelmanagement.entity.Booking;
import com.nguyenhuuquang.hotelmanagement.entity.ChatMessage;
import com.nguyenhuuquang.hotelmanagement.entity.Promotion;
import com.nguyenhuuquang.hotelmanagement.entity.Room;
import com.nguyenhuuquang.hotelmanagement.entity.RoomType;
import com.nguyenhuuquang.hotelmanagement.repository.BookingRepository;
import com.nguyenhuuquang.hotelmanagement.repository.ChatMessageRepository;
import com.nguyenhuuquang.hotelmanagement.repository.PromotionRepository;
import com.nguyenhuuquang.hotelmanagement.repository.RoomRepository;
import com.nguyenhuuquang.hotelmanagement.repository.RoomTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatMessageRepository chatMessageRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final PromotionRepository promotionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public ChatResponse sendMessage(ChatRequest request) {
        try {
            log.info("📤 Sending message to Gemini AI: {}", request.getMessage());

            String systemContext = buildSystemContext();

            String fullPrompt = systemContext + "\n\n" +
                    "Câu hỏi của khách: " + request.getMessage() + "\n\n" +
                    "Hãy trả lời dựa trên thông tin hệ thống ở trên. Nếu không có thông tin, hãy nói là bạn sẽ kiểm tra và liên hệ lại.";

            String aiResponse = callGeminiAPI(fullPrompt);

            ChatMessage chatMessage = ChatMessage.builder()
                    .userMessage(request.getMessage())
                    .aiResponse(aiResponse)
                    .userId(request.getUserId())
                    .build();

            chatMessage = chatMessageRepository.save(chatMessage);

            log.info("✅ AI Response generated and saved successfully");

            return ChatResponse.builder()
                    .id(chatMessage.getId())
                    .userMessage(chatMessage.getUserMessage())
                    .aiResponse(chatMessage.getAiResponse())
                    .timestamp(chatMessage.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("❌ Error in chatbot service: ", e);
            throw new RuntimeException("Không thể kết nối với AI chatbot: " + e.getMessage());
        }
    }

    private String buildSystemContext() {
        StringBuilder context = new StringBuilder();

        context.append("=== THÔNG TIN HỆ THỐNG QUẢN LY KHÁCH SẠN ===\n\n");

        try {
            List<Room> availableRooms = roomRepository.findByStatus(
                    com.nguyenhuuquang.hotelmanagement.entity.enums.RoomStatus.AVAILABLE);
            context.append("📊 PHÒNG TRỐNG HIỆN TẠI:\n");
            if (availableRooms.isEmpty()) {
                context.append("- Hiện tại không có phòng trống.\n");
            } else {
                for (Room room : availableRooms) {
                    context.append(String.format("- Phòng %s (Loại: %s, Giá: %,.0f VNĐ/đêm)\n",
                            room.getRoomNumber(),
                            room.getRoomType() != null ? room.getRoomType().getName() : "N/A",
                            room.getPrice()));
                }
            }
            context.append("\n");
        } catch (Exception e) {
            log.warn("⚠️ Error loading available rooms: {}", e.getMessage());
        }

        try {
            List<RoomType> roomTypes = roomTypeRepository.findAll();
            context.append("🏨 CÁC LOẠI PHÒNG:\n");
            for (RoomType type : roomTypes) {
                context.append(String.format("- %s: %,.0f VNĐ/đêm - %s\n",
                        type.getName(),
                        type.getBasePrice(),
                        type.getDescription() != null ? type.getDescription() : ""));
            }
            context.append("\n");
        } catch (Exception e) {
            log.warn("⚠️ Error loading room types: {}", e.getMessage());
        }

        try {
            LocalDate today = LocalDate.now();
            List<Promotion> activePromotions = promotionRepository.findByActive(true).stream()
                    .filter(p -> (p.getStartDate() == null || !p.getStartDate().isAfter(today)) &&
                            (p.getEndDate() == null || !p.getEndDate().isBefore(today)))
                    .collect(Collectors.toList());

            context.append("🎁 KHUYẾN MÃI ĐANG ÁP DỤNG:\n");
            if (activePromotions.isEmpty()) {
                context.append("- Hiện tại không có chương trình khuyến mãi.\n");
            } else {
                for (Promotion promo : activePromotions) {
                    String discountInfo = "";
                    if (promo.getType() != null && promo.getValue() != null) {
                        if (promo.getType().toString().equals("PERCENTAGE")) {
                            discountInfo = promo.getValue() + "%";
                        } else {
                            discountInfo = String.format("%,.0f VNĐ", promo.getValue());
                        }
                    }

                    context.append(String.format("- %s (Mã: %s): Giảm %s - %s\n",
                            promo.getName(),
                            promo.getCode(),
                            discountInfo,
                            promo.getDescription() != null ? promo.getDescription() : ""));
                }
            }
            context.append("\n");
        } catch (Exception e) {
            log.warn("⚠️ Error loading promotions: {}", e.getMessage());
        }

        try {
            LocalDate today = LocalDate.now();
            List<Booking> todayCheckIns = bookingRepository.findAll().stream()
                    .filter(b -> b.getCheckIn() != null && b.getCheckIn().equals(today))
                    .collect(Collectors.toList());

            List<Booking> todayCheckOuts = bookingRepository.findAll().stream()
                    .filter(b -> b.getCheckOut() != null && b.getCheckOut().equals(today))
                    .collect(Collectors.toList());

            context.append("📅 THỐNG KÊ HÔM NAY:\n");
            context.append(String.format("- Số lượng khách check-in: %d\n", todayCheckIns.size()));
            context.append(String.format("- Số lượng khách check-out: %d\n", todayCheckOuts.size()));
            context.append("\n");
        } catch (Exception e) {
            log.warn("⚠️ Error loading booking statistics: {}", e.getMessage());
        }

        context.append("=== HƯỚNG DẪN TRẢ LỜI ===\n");
        context.append("- Bạn là trợ lý AI thông minh cho hệ thống quản lý khách sạn.\n");
        context.append("- Trả lời dựa trên thông tin thực tế từ hệ thống ở trên.\n");
        context.append("- Nếu khách hỏi về phòng trống, giá phòng, khuyến mãi -> dùng thông tin ở trên.\n");
        context.append("- Nếu khách muốn đặt phòng -> hướng dẫn họ liên hệ lễ tân hoặc đặt qua app.\n");
        context.append("- Trả lời chuyên nghiệp, thân thiện và hữu ích.\n");
        context.append("- KHÔNG đưa ra thông tin không có trong hệ thống.\n\n");

        return context.toString();
    }

    @SuppressWarnings("unchecked")
    private String callGeminiAPI(String prompt) {
        try {
            String url = geminiApiUrl + "?key=" + geminiApiKey;
            log.info("🌐 Calling Gemini API");

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            log.info("✅ Gemini API responded with status: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    List<Map<String, String>> partsList = (List<Map<String, String>>) contentMap.get("parts");
                    if (!partsList.isEmpty()) {
                        String aiText = partsList.get(0).get("text");
                        log.info("✅ AI response extracted successfully");
                        return aiText;
                    }
                }
            }

            log.warn("⚠️ No valid response from Gemini API");
            return "Xin lỗi, tôi không thể trả lời câu hỏi này.";

        } catch (HttpClientErrorException e) {
            log.error("❌ Gemini API Client Error (4xx): Status={}, Body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            int statusCode = e.getStatusCode().value();
            switch (statusCode) {
                case 400:
                    return "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại câu hỏi.";
                case 403:
                    return "API key không hợp lệ hoặc đã hết hạn. Vui lòng liên hệ quản trị viên.";
                case 404:
                    return "Model AI không tồn tại. Vui lòng kiểm tra cấu hình.";
                case 429:
                    return "Đã vượt quá giới hạn số lượng yêu cầu. Vui lòng thử lại sau.";
                default:
                    return "Đã xảy ra lỗi khi xử lý yêu cầu. Vui lòng thử lại sau.";
            }

        } catch (HttpServerErrorException e) {
            log.error("❌ Gemini API Server Error (5xx): Status={}, Body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return "Máy chủ AI đang gặp sự cố. Vui lòng thử lại sau.";

        } catch (Exception e) {
            log.error("❌ Unexpected error calling Gemini API: ", e);
            return "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.";
        }
    }

    public List<ChatResponse> getChatHistory(String userId) {
        try {
            log.info("📜 Fetching chat history for user: {}", userId);
            List<ChatMessage> messages = chatMessageRepository.findByUserIdOrderByCreatedAtDesc(userId);
            log.info("✅ Found {} messages for user", messages.size());

            return messages.stream()
                    .map(msg -> ChatResponse.builder()
                            .id(msg.getId())
                            .userMessage(msg.getUserMessage())
                            .aiResponse(msg.getAiResponse())
                            .timestamp(msg.getCreatedAt())
                            .build())
                    .toList();
        } catch (Exception e) {
            log.error("❌ Error fetching chat history: ", e);
            return new ArrayList<>();
        }
    }
}