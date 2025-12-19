package backend.service;

import backend.rag.InMemoryVectorStore;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class RAGQueryService {
    private final InMemoryVectorStore vectorStore;

    public RAGQueryService(InMemoryVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String queryDocuments(String question) {
        // Simple cosine similarity search (dummy implementation)
        // In real RAG, you'd use proper vector similarity search

        Map<String, float[]> vectors = vectorStore.getVectors();

        if (vectors.isEmpty()) {
            return "I don't have any documents to search through. " +
                    "Please upload some documents first using the Upload page.";
        }

        // Dummy search - just return first document's content
        Map<String, String> contents = vectorStore.getContents();
        if (!contents.isEmpty()) {
            String firstDocId = contents.keySet().iterator().next();
            String content = contents.get(firstDocId);

            // Extract first 200 chars as context
            String context = content.length() > 200 ?
                    content.substring(0, 200) + "..." : content;

            return "Based on your documents, here's what I found:\n\n" +
                    "Context: " + context + "\n\n" +
                    "Answer to your question \"" + question + "\": " +
                    "This is a dummy response. In a real RAG system, " +
                    "I would search through all documents and generate " +
                    "a proper answer using an LLM.";
        }

        return "I found documents but couldn't retrieve the content. " +
                "Please try uploading the documents again.";
    }
}