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

    public PersistentVectorStore(String storagePath) {
        this.storageFile = Path.of(storagePath, "vectors.json");
        loadFromDisk();
    }

    @Override
    public void addVector(String id, String content, float[] embedding) {
        vectors.put(id, new VectorEntry(id, content, embedding));
        saveToDisk();
    }

    @Override
    public List<SearchResult> searchSimilar(float[] queryEmbedding, int topK) {
        List<SearchResult> results = new ArrayList<>();

        for (VectorEntry entry : vectors.values()) {
            float similarity = cosineSimilarity(queryEmbedding, entry.embedding);
            results.add(new SearchResult(entry.id, entry.content, similarity));
        }

        results.sort((a, b) -> Float.compare(b.similarity, a.similarity));
        return results.stream().limit(topK).toList();
    }

    @Override
    public List<String> getContents() {
        return vectors.keySet().stream().toList();
    }

    @Override
    public int getVectorCount() {
        return vectors.size();
    }

    private void saveToDisk() {
        try {
            Files.createDirectories(storageFile.getParent());
            mapper.writeValue(storageFile.toFile(), vectors);
        } catch (Exception e) {
            System.err.println("❌ Failed to save vectors: " + e.getMessage());
        }
    }

    private void loadFromDisk() {
        try {
            if (Files.exists(storageFile)) {
                Map<String, VectorEntry> loaded =
                        mapper.readValue(storageFile.toFile(), mapper.getTypeFactory()
                                .constructMapType(HashMap.class, String.class, VectorEntry.class));

                vectors.putAll(loaded);
                System.out.println("📦 Loaded " + vectors.size() + " vectors from disk");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to load vectors: " + e.getMessage());
        }
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

