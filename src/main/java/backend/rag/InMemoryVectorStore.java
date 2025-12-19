package backend.rag;

import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class InMemoryVectorStore implements VectorStore {
    private final Map<String, float[]> vectors = new HashMap<>();
    private final Map<String, String> contents = new HashMap<>();

    @Override
    public void store(String id, String content, float[] embedding) {
        vectors.put(id, embedding);
        contents.put(id, content);
        System.out.println("✅ Stored document ID: " + id);
        System.out.println("   Content preview: " +
                (content.length() > 100 ? content.substring(0, 100) + "..." : content));
        System.out.println("   Embedding dimensions: " + embedding.length);
        System.out.println("   Total documents: " + vectors.size());
    }

    @Override
    public List<SearchResult> searchSimilar(float[] queryEmbedding, int topK) {
        System.out.println("🔍 Searching through " + vectors.size() + " documents...");

        if (vectors.isEmpty()) {
            System.out.println("⚠️ No documents in vector store!");
            return new ArrayList<>();
        }

        List<SearchResult> results = new ArrayList<>();

        for (Map.Entry<String, float[]> entry : vectors.entrySet()) {
            String docId = entry.getKey();
            float[] docEmbedding = entry.getValue();

            // Calculate cosine similarity
            float similarity = cosineSimilarity(queryEmbedding, docEmbedding);

            results.add(new SearchResult(
                    docId,
                    contents.get(docId),
                    similarity
            ));
        }

        // Sort by similarity (highest first)
        results.sort((a, b) -> Float.compare(b.similarity, a.similarity));

        // Return top K results
        int returnCount = Math.min(topK, results.size());
        List<SearchResult> topResults = results.subList(0, returnCount);

        System.out.println("   Found " + returnCount + " similar documents");
        if (!topResults.isEmpty()) {
            System.out.println("   Best similarity: " +
                    String.format("%.2f", topResults.get(0).similarity * 100) + "%");
        }

        return topResults;
    }

    private float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector dimensions don't match!");
        }

        float dot = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0f;
        }

        return (float)(dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    @Override
    public Map<String, float[]> getVectors() {
        return new HashMap<>(vectors);
    }

    @Override
    public Map<String, String> getContents() {
        return new HashMap<>(contents);
    }

    @Override
    public void clear() {
        vectors.clear();
        contents.clear();
        System.out.println("🧹 Vector store cleared");
    }

    public int getDocumentCount() {
        return vectors.size();
    }
}