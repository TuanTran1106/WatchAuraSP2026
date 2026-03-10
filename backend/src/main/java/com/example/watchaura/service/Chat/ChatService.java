package com.example.watchaura.service.Chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {

    @Value("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")
    private String groqApiKey;

    @Value("${openai.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WatchKnowledgeBase knowledgeBase;

    public ChatService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.knowledgeBase = new WatchKnowledgeBase();
    }

    public String processMessage(String userMessage) {
        // Debug log
        System.out.println("=== GROQ API DEBUG ===");
        System.out.println("API Key có giá trị: " + (groqApiKey != null && !groqApiKey.trim().isEmpty()));
        System.out.println("API URL: " + groqApiUrl);
        System.out.println("User Message: " + userMessage);

        //nếu không có API key, sử dụng rule-based response
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            System.out.println("Không có API key, sử dụng offline mode");
            return knowledgeBase.getResponse(userMessage);
        }

        try {
            // Gọi Groq API
            String aiResponse = callGroqAPI(userMessage);
            System.out.println("Groq AI Response: " + aiResponse);
            return aiResponse;
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Groq API: " + e.getMessage());
            e.printStackTrace();
            // Fallback về rule-based nếu AI API lỗi
            return knowledgeBase.getResponse(userMessage);
        }
    }

    private String callGroqAPI(String userMessage) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-8b-instant"); // Hoặc "mixtral-8x7b-32768"
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        //prompt
        String systemPrompt = """
                Bạn là một chuyên gia tư vấn đồng hồ cao cấp và thời trang. Nhiệm vụ của bạn là cung cấp thông tin chính xác, dễ hiểu, và thân thiện với người dùng. 
                Khi trả lời, bạn cần:
                - Trả lời khách hàng hoàn toàn bằng Tiếng Việt,
                - Giải thích rõ ràng, đầy đủ và trung thực về đồng hồ, bao gồm: thương hiệu, chất liệu, kích thước, tính năng, giá cả, chính sách bảo hành.
                - Gợi ý sản phẩm phù hợp với nhu cầu và ngân sách khách hàng (đồng hồ nam, nữ, đôi, thể thao, thời trang, cơ, quartz…).
                - Đưa ra lời khuyên về cách chọn đồng hồ phù hợp với phong cách, mục đích sử dụng và xu hướng thời trang.
                - Nếu khách hàng hỏi về bảo quản, hướng dẫn cách sử dụng và vệ sinh đồng hồ.
                - Luôn xưng hô lịch sự và dùng giọng văn thân thiện.
            """;

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        Map<String, String> userMessageMap = new HashMap<>();
        userMessageMap.put("role", "user");
        userMessageMap.put("content", userMessage);

        requestBody.put("messages", Arrays.asList(systemMessage, userMessageMap));

        System.out.println("Request Body: " + objectMapper.writeValueAsString(requestBody));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(groqApiUrl, entity, String.class);

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        return jsonNode.get("choices").get(0).get("message").get("content").asText().trim();
    }
}