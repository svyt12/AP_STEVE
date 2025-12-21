package backend.service;

import backend.rag.EmbeddingService;
import backend.rag.SearchResult;
import backend.rag.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RAGQueryService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final GPTService gptService;

    public RAGQueryService(VectorStore vectorStore, EmbeddingService embeddingService, GPTService gptService) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        this.gptService = gptService;
    }

    /**
     * Search for documents relevant to the query
     */
    public List<SearchResult> searchDocuments(String query) {
        try {
            System.out.println("🔍 Searching for: " + query);

            // Convert query to embedding
            float[] queryEmbedding = embeddingService.embed(query);

            // Search vector store
            List<SearchResult> results = new ArrayList<>(vectorStore.searchSimilar(queryEmbedding, 10));

            System.out.println("   Found " + results.size() + " potential matches");

            // Filter only highly relevant results (>50% similarity)
            results.removeIf(result -> !result.isHighlyRelevant(0.5f));

            System.out.println("   After filtering: " + results.size() + " highly relevant");

            return results;

        } catch (Exception e) {
            System.err.println("❌ Search failed: " + e.getMessage());
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generate an answer using GPT based on relevant documents
     */
    public String generateAnswer(String question, List<SearchResult> relevantDocs) {
        if (relevantDocs.isEmpty()) {
            return "I couldn't find relevant information in the uploaded documents about '" + question + "'.";
        }

        // Merge top 5 chunks for context
        List<SearchResult> topChunks = relevantDocs.subList(0, Math.min(5, relevantDocs.size()));
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < topChunks.size(); i++) {
            SearchResult doc = topChunks.get(i);
            contextBuilder.append(i + 1).append(". [")
                    .append(String.format("%.2f", doc.getSimilarityPercentage()))
                    .append("% match] From ")
                    .append(doc.getDocumentId())
                    .append(":\n")
                    .append(doc.getContent())
                    .append("\n\n");
        }

        String mergedContext = contextBuilder.toString();

        // code aware gpt prompt
        String prompt = """
                You are a C++ tutor. Use ONLY the context from the uploaded documents.
                - If the context contains code, you may infer variable values or pointer relationships from the code.
                - Answer clearly and concisely.

                Context:
                %s

                Question:
                %s

                Answer:
                """.formatted(mergedContext, question);

        System.out.println("📝 Prompt prepared for GPT:\n" + prompt);

        //call gpt via openai api
        String answer;
        try {
            answer = gptService.chatCompletion(prompt); // wrapper for OpenAI API
        } catch (Exception e) {
            System.err.println("❌ GPT call failed: " + e.getMessage());
            e.printStackTrace();

            // Fallback: show top chunk preview
            answer = "Based on the uploaded documents, here's what I found:\n\n" +
                    topChunks.get(0).getContentPreview(300) +
                    "\n\n💡 Answer summary: The documents contain information about this topic.";
        }

        return answer;
    }

    /**
     * Generate documents for quiz questions (optional)
     */
    public List<SearchResult> generateQuizQuestions(String topic, int numberOfQuestions) {
        List<SearchResult> topicDocs = searchDocuments(topic);

        if (topicDocs.isEmpty()) {
            throw new RuntimeException("No documents found about: " + topic);
        }

        System.out.println("🎯 Found " + topicDocs.size() + " documents for quiz on: " + topic);
        return topicDocs;
    }
}
