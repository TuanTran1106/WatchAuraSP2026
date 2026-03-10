package com.example.watchaura.controller;

import com.example.watchaura.entity.Chat.ChatRequest;
import com.example.watchaura.entity.Chat.ChatResponse;
import com.example.watchaura.service.Chat.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/message")
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        try {
            String response = chatService.processMessage(request.getMessage());
            return new ChatResponse(response, true);
        } catch (Exception e) {
            return new ChatResponse("Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.", false);
        }
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Chatbot service is running!";
    }

    @GetMapping("/message")
    public ChatResponse getMessageTest(@RequestParam(defaultValue = "Xin chào") String msg) {
        try {
            String response = chatService.processMessage(msg);
            return new ChatResponse(response, true);
        } catch (Exception e) {
            return new ChatResponse("Lỗi test GET: " + e.getMessage(), false);
        }
    }
}
