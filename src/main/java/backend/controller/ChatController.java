package backend.controller;

import backend.rag.SearchResult;
import backend.service.RAGQueryService;
import frontend.student.ChatHistoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        String question = request.get("question");
        System.out.println("\n💭 Question received: " + question);

        try {
            // RAG query
            List<SearchResult> relevantDocs = ragService.searchDocuments(question);
            String answer = ragService.generateAnswer(question, relevantDocs);

            System.out.println("✅ Generated answer with " + relevantDocs.size() + " relevant documents");

            // --- Save to persistent chat history ---
            List<String> conversation = new ArrayList<>();
            conversation.add("User: " + question);
            conversation.add("AI: " + answer);
            chatHistoryManager.addChat(question, conversation);

            return Map.of(
                    "answer", answer,
                    "relevantDocuments", relevantDocs,
                    "questionType", "RAG_QUERY",
                    "timestamp", new Date().toString()
            );

        } catch (Exception e) {
            System.err.println("❌ Error processing question: " + e.getMessage());
            e.printStackTrace();
            return Map.of(
                    "answer", "Sorry, I encountered an error: " + e.getMessage(),
                    "error", true,
                    "questionType", "ERROR"
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
