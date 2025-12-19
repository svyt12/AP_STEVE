package frontend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class ChatService {
    private static final String CHAT_URL = "http://localhost:8080/api/chat/ask";
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    public ChatService() {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.chatService = new ChatService();
    }

    public String askQuestion(String question) throws Exception {
        // Create request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("question", question);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // Create request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHAT_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Send request
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Parse response
            Map<String, String> responseMap = objectMapper.readValue(
                    response.body(), Map.class);
            return responseMap.get("answer");
        } else {
            throw new RuntimeException("Chat failed: " + response.statusCode()
                    + " - " + response.body());
        }
    }
}