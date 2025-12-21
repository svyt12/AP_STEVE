package frontend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
// sends the request
public class ChatService {

    private static final String CHAT_URL = "http://localhost:8080/api/chat/ask";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponse askQuestion(String question) {
        try {
            System.out.println("💬 Sending question to backend: " + question);

            // Prepare request
            Map<String, String> request = new HashMap<>();
            request.put("question", question);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // Send request
            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    CHAT_URL,
                    HttpMethod.POST,
                    entity,
                    ChatResponse.class
            );

            System.out.println("✅ Received response from backend");
            return response.getBody();

        } catch (Exception e) {
            System.err.println("❌ Chat request failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get answer: " + e.getMessage(), e);
        }
    }

    // Response DTO class
    public static class ChatResponse {
        private String answer;
        private Object relevantDocuments; // Will contain SearchResult objects

        // Getters and setters
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public Object getRelevantDocuments() { return relevantDocuments; }
        public void setRelevantDocuments(Object relevantDocuments) {
            this.relevantDocuments = relevantDocuments;
        }
    }
}