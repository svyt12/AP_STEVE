package backend.rag;

import java.util.HashMap;
import java.util.Map;

public class InMemoryVectorStore implements VectorStore {
    private final Map<String, float[]> vectors = new HashMap<>();
    private final Map<String, String> contents = new HashMap<>();

    @Override
    public void store(String id, String content, float[] embedding) {
        vectors.put(id, embedding);
        contents.put(id, content);
        System.out.println("Stored document ID: " + id);
        System.out.println("Content preview: " +
                (content.length() > 100 ? content.substring(0, 100) + "..." : content));
    }

    // Add getters for RAG query service
    public Map<String, float[]> getVectors() {
        return new HashMap<>(vectors);
    }

    public Map<String, String> getContents() {
        return new HashMap<>(contents);
    }

    public int getDocumentCount() {
        return vectors.size();
    }
}