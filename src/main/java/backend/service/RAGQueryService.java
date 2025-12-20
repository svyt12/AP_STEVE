package backend.service;

import backend.rag.EmbeddingService;
import backend.rag.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RAGQueryService {
    private final VectorStore vectorStore;  //interface
    private final EmbeddingService embeddingService;

    public RAGQueryService(VectorStore vectorStore, EmbeddingService embeddingService) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        System.out.println("✅ RAGQueryService initialized");
    }

    public String queryDocuments(String question) {
        System.out.println("\n📝 Question: " + question);

        try {
            if (vectorStore.getContents().isEmpty()) {
                return "I don't have any documents to search through. " +
                        "Please upload some PDF documents first using the Upload page.";
            }

            System.out.println("   Documents in store: " + vectorStore.getContents().size());

            System.out.println("   Creating embedding for question...");
            float[] questionEmbedding = embeddingService.embed(question);
            System.out.println("   Embedding created: " + questionEmbedding.length + " dimensions");

            List<VectorStore.SearchResult> similarDocs =
                    vectorStore.searchSimilar(questionEmbedding, 3);

            if (similarDocs.isEmpty()) {
                return "I couldn't find any relevant information in the documents " +
                        "to answer your question: \"" + question + "\"";
            }

            StringBuilder context = new StringBuilder();
            for (VectorStore.SearchResult doc : similarDocs) {
                String excerpt = doc.content.length() > 300 ?
                        doc.content.substring(0, 300) + "..." : doc.content;
                context.append("[Relevance: ")
                        .append(String.format("%.1f", doc.similarity * 100))
                        .append("%] ")
                        .append(excerpt)
                        .append("\n\n");
            }

            // return the found context
            // add OpenAI chat API integration here
            String answer = buildAnswerFromContext(context.toString(), question);

            System.out.println("✅ 3Query completed successfully");
            return answer;

        } catch (Exception e) {
            System.err.println("❌ Error in RAGQueryService: " + e.getMessage());
            e.printStackTrace();
            return "Sorry, there was an error processing your question: " + e.getMessage();
        }
    }

    private String buildAnswerFromContext(String context, String question) {
        // For now, return a simple answer with the context
        // Later you'll integrate OpenAI Chat API here

        return "Based on your documents, here's what I found:\n\n" +
                "Context from documents:\n" + context + "\n" +
                "Question: " + question + "\n\n" +
                "(Note: In the next step, this will be sent to OpenAI for a proper answer)";
    }
}