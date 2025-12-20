package backend.controller;

import backend.rag.SearchResult;
import backend.service.RAGQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private RAGQueryService ragService;

    @GetMapping("/health")
    public String health() {
        return "✅ Chat endpoint is healthy at " + new java.util.Date();
    }

    @PostMapping("/ask")
    public Map<String, Object> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        System.out.println("\n💭 Question received: " + question);

        try {
            // Check if it's a quiz request
            if (question.toLowerCase().contains("quiz")) {
                System.out.println("🎯 Detected quiz request");
                return generateQuiz(question);
            }

            // Regular RAG query
            List<SearchResult> relevantDocs = ragService.searchDocuments(question);
            String answer = ragService.generateAnswer(question, relevantDocs);

            System.out.println("✅ Generated answer with " + relevantDocs.size() + " relevant documents");

            return Map.of(
                    "answer", answer,
                    "relevantDocuments", relevantDocs,
                    "questionType", "RAG_QUERY",
                    "timestamp", new java.util.Date().toString()
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

    private Map<String, Object> generateQuiz(String question) {
        // Extract topic from question
        String topic = extractTopicFromQuestion(question);

        System.out.println("📝 Generating quiz for topic: " + topic);

        // In a real implementation, you would:
        // 1. Search for documents about the topic
        // 2. Generate quiz questions based on content
        // 3. Return the quiz

        return Map.of(
                "answer", "🎯 I'll generate a quiz about: " + topic +
                        "\n\n📚 Quiz Content: [Would be generated from your documents]" +
                        "\n\n1. What is " + topic + "?" +
                        "\n   A) Option A" +
                        "\n   B) Option B" +
                        "\n   C) Option C" +
                        "\n   D) Option D" +
                        "\n\n2. Explain the main concept of " + topic +
                        "\n\n[This is a placeholder - real quiz would come from your documents]",
                "questionType", "QUIZ",
                "quizTopic", topic,
                "isQuiz", true,
                "timestamp", new java.util.Date().toString()
        );
    }

    private String extractTopicFromQuestion(String question) {
        // Simple topic extraction - remove "quiz" and common words
        String cleaned = question.toLowerCase()
                .replace("quiz", "")
                .replace("generate", "")
                .replace("create", "")
                .replace("make", "")
                .replace("about", "")
                .replace("on", "")
                .trim();

        // Capitalize first letter
        if (!cleaned.isEmpty()) {
            return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
        }
        return "the uploaded documents";
    }
}