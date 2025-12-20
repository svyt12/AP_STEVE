package backend.service;

import backend.rag.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RAGQueryService {

    private OpenAiEmbeddingService embeddingService;

    private InMemoryVectorStore vectorStore;

    public RAGQueryService(VectorStore vectorStore, EmbeddingService embeddingService) {
        this.vectorStore = (InMemoryVectorStore) vectorStore;
        this.embeddingService = (OpenAiEmbeddingService) embeddingService;
    }

    public List<SearchResult> searchDocuments(String query) {
        try {
            System.out.println("🔍 Searching for: " + query);

            // Convert query to embedding
            float[] queryEmbedding = embeddingService.embed(query);

            // Search vector store
            List<SearchResult> results = vectorStore.searchSimilar(queryEmbedding, 10);

            System.out.println("   Found " + results.size() + " potential matches");

            // Filter only highly relevant results
            results.removeIf(result -> !result.isHighlyRelevant(0.5f));

            System.out.println("   After filtering: " + results.size() + " highly relevant");

            return results;

        } catch (Exception e) {
            System.err.println("❌ Search failed: " + e.getMessage());
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    public String generateAnswer(String question, List<SearchResult> relevantDocs) {
        if (relevantDocs.isEmpty()) {
            return "I couldn't find relevant information in the uploaded documents about '" + question + "'.";
        }

        // Build context from search results
        StringBuilder context = new StringBuilder();
        context.append("Question: ").append(question).append("\n\n");
        context.append("Relevant information from documents:\n\n");

        for (int i = 0; i < relevantDocs.size(); i++) {
            SearchResult doc = relevantDocs.get(i);
            context.append(i + 1).append(". [")
                    .append(doc.getSimilarityPercentage())
                    .append("% match] From ")
                    .append(doc.getDocumentId())
                    .append(":\n")
                    .append(doc.getContent())
                    .append("\n\n");
        }

        System.out.println("📝 Context built with " + relevantDocs.size() + " documents");

        // TODO: Integrate with actual LLM (OpenAI GPT) here
        // For now, return a simple answer

        String bestMatch = relevantDocs.get(0).getContentPreview(300);

        return "Based on the uploaded documents, here's what I found:\n\n" +
                "📚 Most relevant information (" +
                relevantDocs.get(0).getSimilarityPercentage() + "% match):\n" +
                bestMatch + "\n\n" +
                "💡 Answer summary: The documents contain information about this topic. " +
                "For more details, please refer to the specific sections mentioned above.";
    }

    public List<SearchResult> generateQuizQuestions(String topic, int numberOfQuestions) {
        // Search for documents about the topic
        List<SearchResult> topicDocs = searchDocuments(topic);

        if (topicDocs.isEmpty()) {
            throw new RuntimeException("No documents found about: " + topic);
        }

        // TODO: Use LLM to generate quiz questions from the documents
        // For now, return the relevant documents that could be used for quiz generation

        System.out.println("🎯 Found " + topicDocs.size() + " documents for quiz on: " + topic);
        return topicDocs;
    }
}