package backend.controller;

import backend.rag.SearchResult;
import backend.service.RAGQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import frontend.student.ChatHistoryManager;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private RAGQueryService ragService;

    private final ChatHistoryManager chatHistoryManager;

    public ChatController() {
        this.chatHistoryManager = new ChatHistoryManager(); // JSON-based persistent storage
    }

    /** Health check **/
    @GetMapping("/health")
    public String health() {
        return "✅ Chat endpoint is healthy at " + new Date();
    }

    /** Ask a new question **/
    @PostMapping("/ask")
    public Map<String, Object> askQuestion(@RequestBody Map<String, String> request) {
        String rawInput = request.get("question");
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return Map.of("error", true, "message", "Question is empty");
        }

        String input = rawInput.trim();
        boolean isQuiz = false;
        String format = "plain";
        String topic = input;

        // Detect quiz/MCQ
        if (input.toLowerCase().startsWith("mcq ")) {
            isQuiz = true;
            format = "mcq";
            topic = input.substring(4).trim(); // remove "mcq " prefix
        } else if (input.toLowerCase().startsWith("quiz ")) {
            isQuiz = true;
            format = "plain";
            topic = input.substring(5).trim(); // remove "quiz " prefix
        }

        try {
            if (isQuiz) {
                // Generate quiz
                List<String> questions = ragService.generateQuiz(topic, format, 5);

                // Save quiz to chat history
                List<String> conversation = new ArrayList<>();
                conversation.add("User: " + input);
                conversation.addAll(questions.stream().map(q -> "Quiz: " + q).toList());
                chatHistoryManager.addChat(input, conversation);

                return Map.of(
                        "questionType", format.equals("mcq") ? "MCQ_QUIZ" : "PLAIN_QUIZ",
                        "topic", topic,
                        "questions", questions,
                        "timestamp", new Date().toString()
                );

            } else {
                // Normal RAG question
                List<SearchResult> relevantDocs = ragService.searchDocuments(input);
                String answer = ragService.generateAnswer(input, relevantDocs);

                // Save to chat history
                List<String> conversation = new ArrayList<>();
                conversation.add("User: " + input);
                conversation.add("S.T.E.V.E: " + answer);
                chatHistoryManager.addChat(input, conversation);

                return Map.of(
                        "questionType", "RAG_QUERY",
                        "answer", answer,
                        "relevantDocuments", relevantDocs,
                        "timestamp", new Date().toString()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "error", true,
                    "message", "Failed to process request: " + e.getMessage()
            );
        }
    }

    /** Get all chat history (just the query names) **/
    @GetMapping("/history")
    public Set<String> getAllChatHistory() {
        return chatHistoryManager.getAllChats().keySet();
    }

    /** Get full conversation by query **/
    @GetMapping("/history/{query}")
    public List<String> getChat(@PathVariable String query) {
        List<String> conversation = chatHistoryManager.getChat(query);
        if (conversation == null) {
            return Collections.singletonList("No chat found for query: " + query);
        }
        return conversation;
    }
}
