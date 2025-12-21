package frontend.services;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that communicates with backend chat API
 * Handles both RAG queries and quiz/MCQ requests.
 */
public class ChatService {

    private static final String BASE_URL = "http://localhost:8080/api/chat";
    private static final String CHAT_URL = BASE_URL + "/ask";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send a question (normal query or quiz/MCQ) to backend
     *
     * @param input User input (e.g., "mcq climate change" or "quiz climate change" or normal question)
     * @return ChatResponse containing either answer or questions
     */
    public ChatResponse askQuestion(String input) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("question", input);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    CHAT_URL,
                    HttpMethod.POST,
                    entity,
                    ChatResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            System.err.println("❌ Chat request failed: " + e.getMessage());
            throw new RuntimeException("Failed to get answer: " + e.getMessage(), e);
        }
    }

    /** --- Response DTO --- **/
    public static class ChatResponse {
        private String questionType; // "RAG_QUERY", "PLAIN_QUIZ", "MCQ_QUIZ"
        private String answer;       // normal RAG answer
        private List<String> questions; // quiz questions
        private String topic;        // topic for quiz
        private String timestamp;

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public List<String> getQuestions() { return questions; }
        public void setQuestions(List<String> questions) { this.questions = questions; }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
