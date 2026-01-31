package com.nguyenhuuquang.hotelmanagement.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nguyenhuuquang.hotelmanagement.dto.ChatRequest;
import com.nguyenhuuquang.hotelmanagement.dto.ChatResponse;
import com.nguyenhuuquang.hotelmanagement.entity.ChatMessage;
import com.nguyenhuuquang.hotelmanagement.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatMessageRepository chatMessageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public ChatResponse sendMessage(ChatRequest request) {
        try {
            log.info("📤 Sending message to Gemini AI: {}", request.getMessage());

            String contextPrompt = "Bạn là trợ lý AI thông minh cho hệ thống quản lý khách sạn. " +
                    "Hãy trả lời câu hỏi của người dùng một cách chuyên nghiệp, thân thiện và hữu ích. " +
                    "Nếu câu hỏi liên quan đến đặt phòng, thanh toán, dịch vụ khách sạn, hãy cung cấp thông tin chi tiết.\n\n"
                    +
                    "Câu hỏi: " + request.getMessage();

            String aiResponse = callGeminiAPI(contextPrompt);

            ChatMessage chatMessage = ChatMessage.builder()
                    .userMessage(request.getMessage())
                    .aiResponse(aiResponse)
                    .userId(request.getUserId())
                    .build();

            chatMessage = chatMessageRepository.save(chatMessage);

            log.info("✅ AI Response generated and saved");

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

    private String callGeminiAPI(String prompt) {
        try {
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            // Request body theo format Gemini API
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

            // Parse response
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    List<Map<String, String>> partsList = (List<Map<String, String>>) contentMap.get("parts");
                    if (!partsList.isEmpty()) {
                        return partsList.get(0).get("text");
                    }
                }
            }

            return "Xin lỗi, tôi không thể trả lời câu hỏi này.";

        } catch (Exception e) {
            log.error("❌ Gemini API error: ", e);
            return "Đã xảy ra lỗi khi kết nối với AI. Vui lòng thử lại sau.";
        }
    }

    public List<ChatResponse> getChatHistory(String userId) {
        List<ChatMessage> messages = chatMessageRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return messages.stream()
                .map(msg -> ChatResponse.builder()
                        .id(msg.getId())
                        .userMessage(msg.getUserMessage())
                        .aiResponse(msg.getAiResponse())
                        .timestamp(msg.getCreatedAt())
                        .build())
                .toList();
    }
}