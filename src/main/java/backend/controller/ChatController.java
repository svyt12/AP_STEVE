package backend.controller;

import backend.service.RAGQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final RAGQueryService ragQueryService;

    @Autowired
    public ChatController(RAGQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");

            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Question cannot be empty"));
            }

            // Process question through RAG
            String answer = ragQueryService.queryDocuments(question);

            // Return response
            Map<String, String> response = new HashMap<>();
            response.put("question", question);
            response.put("answer", answer);
            response.put("timestamp", String.valueOf(System.currentTimeMillis()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process question: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "S.T.E.V.E RAG Chat");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
}