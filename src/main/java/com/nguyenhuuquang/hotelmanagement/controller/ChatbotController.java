package com.nguyenhuuquang.hotelmanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenhuuquang.hotelmanagement.dto.ChatRequest;
import com.nguyenhuuquang.hotelmanagement.dto.ChatResponse;
import com.nguyenhuuquang.hotelmanagement.service.ChatbotService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(@PathVariable String userId) {
        List<ChatResponse> history = chatbotService.getChatHistory(userId);
        return ResponseEntity.ok(history);
    }
}