package backend.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryVectorStore implements VectorStore {

    private static class VectorEntry implements Serializable {
        String documentId;
        String content;
        float[] embedding;

        VectorEntry(String documentId, String content, float[] embedding) {
            this.documentId = documentId;
            this.content = content;
            this.embedding = embedding;
        }
    }

    private final Map<String, VectorEntry> vectors = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${vectorstore.persistence.file:vectorstore.json}")
    private String storageFile;

    @Value("${vectorstore.persistence.enabled:true}")
    private boolean persistenceEnabled;

    public InMemoryVectorStore() {
        // Try to load existing data on startup
        loadFromStorage();
    }

    @Override
    public void addVector(String documentId, String content, float[] embedding) {
        vectors.put(documentId, new VectorEntry(documentId, content, embedding));

        // Auto-save if persistence is enabled
        if (persistenceEnabled) {
            saveToStorage();
        }
    }

    @Override
    public List<SearchResult> searchSimilar(float[] queryEmbedding, int topK) {
        List<SearchResult> results = new ArrayList<>();

        for (VectorEntry entry : vectors.values()) {
            float similarity = cosineSimilarity(queryEmbedding, entry.embedding);
            results.add(new SearchResult(entry.documentId, entry.content, similarity));
        }

        // Sort by similarity (descending)
        results.sort((a, b) -> Float.compare(b.getSimilarity(), a.getSimilarity()));

        // Return top K results
        return results.subList(0, Math.min(topK, results.size()));
    }

    @Override
    public void saveToFile(String filePath) {
        try {
            Map<String, Object> storageData = new HashMap<>();
            List<Map<String, Object>> entries = new ArrayList<>();

            for (VectorEntry entry : vectors.values()) {
                Map<String, Object> entryMap = new HashMap<>();
                entryMap.put("documentId", entry.documentId);
                entryMap.put("content", entry.content);
                entryMap.put("embedding", entry.embedding);
                entries.add(entryMap);
            }

            storageData.put("vectors", entries);
            storageData.put("timestamp", new Date().toString());
            storageData.put("count", vectors.size());

            objectMapper.writeValue(new File(filePath), storageData);
            System.out.println("💾 VectorStore saved to: " + filePath + " (" + vectors.size() + " vectors)");

        } catch (IOException e) {
            System.err.println("❌ Failed to save VectorStore: " + e.getMessage());
        }
    }

    @Override
    public void loadFromFile(String filePath) {
        try {
            // null check
            if (filePath == null || filePath.trim().isEmpty()) {
                System.out.println("📁 No storage file specified, starting fresh");
                return;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("📁 No storage file found: " + filePath);
                return;
            }

            Map<String, Object> storageData = objectMapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> entries = (List<Map<String, Object>>) storageData.get("vectors");

            vectors.clear();
            for (Map<String, Object> entryMap : entries) {
                String documentId = (String) entryMap.get("documentId");
                String content = (String) entryMap.get("content");

                // Handle float array (JSON array to float[])
                List<Number> embeddingList = (List<Number>) entryMap.get("embedding");
                float[] embedding = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embedding[i] = embeddingList.get(i).floatValue();
                }

                vectors.put(documentId, new VectorEntry(documentId, content, embedding));
            }

            System.out.println("VectorStore loaded from: " + filePath + " (" + vectors.size() + " vectors)");

        } catch (IOException e) {
            System.err.println("❌ Failed to load VectorStore: " + e.getMessage());
        }
    }

    @Override
    public int getVectorCount() {
        return vectors.size();
    }

    @Override
    public Map<String, String> getVectors() {
        Map<String, String> result = new HashMap<>();
        for (VectorEntry entry : vectors.values()) {
            result.put(entry.documentId, entry.content);
        }
        return result;
    }

    @Override
    public void clear() {
        vectors.clear();
        if (persistenceEnabled) {
            saveToStorage();
        }
    }

    public boolean containsDocument(String documentId) {
        return vectors.containsKey(documentId);
    }

    // Additional useful methods
    public List<String> getAllDocumentIds() {
        return new ArrayList<>(vectors.keySet());
    }

    public String getDocumentContent(String documentId) {
        VectorEntry entry = vectors.get(documentId);
        return entry != null ? entry.content : null;
    }

    public float[] getDocumentEmbedding(String documentId) {
        VectorEntry entry = vectors.get(documentId);
        return entry != null ? entry.embedding : null;
    }

    // Helper methods
    private void saveToStorage() {
        saveToFile(storageFile);
    }

    private void loadFromStorage() {
        loadFromFile(storageFile);
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dotProduct / (float)(Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Called when application shuts down
    public void onShutdown() {
        if (persistenceEnabled) {
            saveToStorage();
        }
    }
}