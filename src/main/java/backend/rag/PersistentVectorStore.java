package backend.rag;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PersistentVectorStore implements VectorStore {

    private final Map<String, VectorEntry> vectors = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path storageFile;

    public PersistentVectorStore(String basePath) {
        this.storageFile = Path.of(basePath, "vectors.json");
        loadFromFile(storageFile.toString());
    }


    @Override
    public void addVector(String documentId, String content, float[] embedding) {
        vectors.put(documentId, new VectorEntry(documentId, content, embedding));
        saveToFile(storageFile.toString());
    }

    @Override
    public List<SearchResult> searchSimilar(float[] queryEmbedding, int topK) {
        List<SearchResult> results = new ArrayList<>();

        for (VectorEntry entry : vectors.values()) {
            float similarity = cosineSimilarity(queryEmbedding, entry.embedding);
            results.add(new SearchResult(entry.id, entry.content, similarity));
        }

        results.sort((a, b) -> Float.compare(b.getSimilarity(), a.getSimilarity()));
        return results.stream().limit(topK).toList();
    }

    @Override
    public int getVectorCount() {
        return vectors.size();
    }

    @Override
    public Map<String, String> getVectors() {
        Map<String, String> contents = new HashMap<>();
        for (VectorEntry entry : vectors.values()) {
            contents.put(entry.id, entry.content);
        }
        return contents;
    }

    // persistence

    @Override
    public void saveToFile(String filePath) {
        try {
            Files.createDirectories(Path.of(filePath).getParent());
            mapper.writeValue(new File(filePath), vectors);
        } catch (Exception e) {
            System.err.println("❌ Failed to save vectors: " + e.getMessage());
        }
    }

    @Override
    public void loadFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Map<String, VectorEntry> loaded =
                        mapper.readValue(file,
                                mapper.getTypeFactory().constructMapType(
                                        HashMap.class, String.class, VectorEntry.class));

                vectors.clear();
                vectors.putAll(loaded);
                System.out.println("📦 Loaded " + vectors.size() + " vectors from disk");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to load vectors: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        vectors.clear();
        saveToFile(storageFile.toString());
        System.out.println("🧹 Vector store cleared");
    }


    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10));
    }


    static class VectorEntry {
        public String id;
        public String content;
        public float[] embedding;

        public VectorEntry() {}
        public VectorEntry(String id, String content, float[] embedding) {
            this.id = id;
            this.content = content;
            this.embedding = embedding;
        }
    }
}
