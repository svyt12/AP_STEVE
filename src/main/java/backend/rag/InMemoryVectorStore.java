package backend.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Component
public class InMemoryVectorStore implements VectorStore {

    // Storage in memory
    private Map<String, float[]> vectors = new HashMap<>();
    private Map<String, String> contents = new HashMap<>();

    // Persistence configuration
    private static final String STORAGE_DIR = "vector_store_data";
    private static final String VECTORS_FILE = "vectors.json.gz";
    private static final String CONTENTS_FILE = "contents.json.gz";
    private static final String METADATA_FILE = "metadata.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean autoSave = true;

    // Constructor - loads data from disk on startup
    public InMemoryVectorStore() {
        loadFromDisk();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveToDisk));
        System.out.println("💾 VectorStore initialized with " + vectors.size() + " vectors");
    }

    @Override
    public void store(String id, String content, float[] embedding) {
        // Store in memory
        vectors.put(id, embedding);
        contents.put(id, content);

        System.out.println("✅ Stored document ID: " + id);
        System.out.println("   Content preview: " +
                (content.length() > 100 ? content.substring(0, 100) + "..." : content));
        System.out.println("   Embedding dimensions: " + embedding.length);
        System.out.println("   Total documents: " + vectors.size());

        // Auto-save to disk if enabled
        if (autoSave) {
            saveToDisk();
        }
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
        results.sort((a, b) -> Float.compare(b.getSimilarity(), a.getSimilarity()));

        // Return top K results
        int returnCount = Math.min(topK, results.size());
        List<SearchResult> topResults = results.subList(0, returnCount);

        System.out.println("   Found " + returnCount + " similar documents");
        if (!topResults.isEmpty()) {
            System.out.println("   Best similarity: " +
                    String.format("%.2f", topResults.get(0).getSimilarity() * 100) + "%");
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
        System.out.println("Vector store cleared");
        saveToDisk(); // Also clear from disk
    }

    public int getDocumentCount() {
        return vectors.size();
    }

    // ========== PERSISTENCE METHODS ==========

    /**
     * Save all data to disk
     */
    public synchronized void saveToDisk() {
        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(STORAGE_DIR);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
                System.out.println("📁 Created storage directory: " + storageDir.toAbsolutePath());
            }

            // Save vectors (compressed JSON)
            saveMapToCompressedJson(vectors, storageDir.resolve(VECTORS_FILE));

            // Save contents (compressed JSON)
            saveMapToCompressedJson(contents, storageDir.resolve(CONTENTS_FILE));

            // Save metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("totalVectors", vectors.size());
            metadata.put("totalContentChars", contents.values().stream().mapToInt(String::length).sum());
            metadata.put("lastSaved", new Date().toString());
            metadata.put("autoSaveEnabled", autoSave);

            objectMapper.writeValue(storageDir.resolve(METADATA_FILE).toFile(), metadata);

            System.out.println("Saved " + vectors.size() + " vectors to disk");

        } catch (Exception e) {
            System.err.println("❌ Failed to save to disk: " + e.getMessage());
        }
    }

    /**
     * Load data from disk
     */
    @SuppressWarnings("unchecked")
    public synchronized void loadFromDisk() {
        try {
            Path storageDir = Paths.get(STORAGE_DIR);
            if (!Files.exists(storageDir)) {
                System.out.println("📁 No storage directory found, starting fresh");
                return;
            }

            // Load vectors
            Path vectorsFile = storageDir.resolve(VECTORS_FILE);
            if (Files.exists(vectorsFile)) {
                Map<String, List<Double>> vectorsFromDisk = loadMapFromCompressedJson(
                        vectorsFile, new TypeReference<Map<String, List<Double>>>() {});

                // Convert List<Double> to float[]
                vectors = new HashMap<>();
                for (Map.Entry<String, List<Double>> entry : vectorsFromDisk.entrySet()) {
                    float[] floatArray = new float[entry.getValue().size()];
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        floatArray[i] = entry.getValue().get(i).floatValue();
                    }
                    vectors.put(entry.getKey(), floatArray);
                }
            }

            // Load contents
            Path contentsFile = storageDir.resolve(CONTENTS_FILE);
            if (Files.exists(contentsFile)) {
                contents = loadMapFromCompressedJson(
                        contentsFile, new TypeReference<Map<String, String>>() {});
            }

            // Load metadata
            Path metadataFile = storageDir.resolve(METADATA_FILE);
            if (Files.exists(metadataFile)) {
                Map<String, Object> metadata = objectMapper.readValue(
                        metadataFile.toFile(), new TypeReference<Map<String, Object>>() {});

                System.out.println("Previously saved: " +
                        metadata.get("totalVectors") + " vectors, last saved: " +
                        metadata.get("lastSaved"));
            }

            System.out.println("Loaded " + vectors.size() + " vectors from disk");

        } catch (Exception e) {
            System.err.println("⚠️ Could not load from disk: " + e.getMessage());
            System.out.println("   Starting with empty vector store");
            vectors = new HashMap<>();
            contents = new HashMap<>();
        }
    }

    /**
     * Save a map to compressed JSON file
     */
    private <T> void saveMapToCompressedJson(Map<String, T> map, Path filePath) throws IOException {
        // Convert float[] to List<Double> for JSON serialization
        if (!map.isEmpty() && map.values().iterator().next() instanceof float[]) {
            Map<String, List<Double>> serializableMap = new HashMap<>();
            for (Map.Entry<String, T> entry : map.entrySet()) {
                float[] floatArray = (float[]) entry.getValue();
                List<Double> doubleList = new ArrayList<>();
                for (float f : floatArray) {
                    doubleList.add((double) f);
                }
                serializableMap.put(entry.getKey(), doubleList);
            }

            try (OutputStream fos = Files.newOutputStream(filePath);
                 GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
                objectMapper.writeValue(gzos, serializableMap);
            }
        } else {
            try (OutputStream fos = Files.newOutputStream(filePath);
                 GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
                objectMapper.writeValue(gzos, map);
            }
        }
    }

    /**
     * Load a map from compressed JSON file
     */
    private <T> Map<String, T> loadMapFromCompressedJson(Path filePath, TypeReference<Map<String, T>> typeRef)
            throws IOException {
        try (InputStream fis = Files.newInputStream(filePath);
             GZIPInputStream gzis = new GZIPInputStream(fis)) {
            return objectMapper.readValue(gzis, typeRef);
        }
    }

    // ========== UTILITY METHODS ==========

    /**
     * Enable/disable auto-save
     */
    public void setAutoSave(boolean enabled) {
        this.autoSave = enabled;
        System.out.println("⚙️ Auto-save " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Get storage statistics
     */
    public void printStorageStats() {
        System.out.println("\nVector Store Statistics:");
        System.out.println("   Memory:");
        System.out.println("     - Total chunks: " + vectors.size());
        System.out.println("     - Total content characters: " +
                contents.values().stream().mapToInt(String::length).sum());
        System.out.println("     - Estimated memory: " + estimateMemoryUsage() + " MB");

        // Disk info
        Path storageDir = Paths.get(STORAGE_DIR);
        if (Files.exists(storageDir)) {
            try {
                long diskSize = Files.walk(storageDir)
                        .filter(p -> p.toFile().isFile())
                        .mapToLong(p -> p.toFile().length())
                        .sum();
                System.out.println("   Disk:");
                System.out.println("     - Storage directory: " + storageDir.toAbsolutePath());
                System.out.println("     - Disk usage: " + (diskSize / 1024) + " KB");

                File[] files = storageDir.toFile().listFiles();
                if (files != null) {
                    for (File file : files) {
                        System.out.println("     - " + file.getName() + ": " +
                                (file.length() / 1024) + " KB");
                    }
                }
            } catch (IOException e) {
                System.err.println("   Could not calculate disk usage: " + e.getMessage());
            }
        }

        // Show sample entries
        if (!vectors.isEmpty()) {
            System.out.println("   Sample entries:");
            int count = 0;
            for (Map.Entry<String, String> entry : contents.entrySet()) {
                if (count++ < 2) {
                    System.out.println("     - " + entry.getKey() + ": " +
                            entry.getValue().substring(0, Math.min(60, entry.getValue().length())) + "...");
                }
            }
        }
    }

    /**
     * Estimate memory usage
     */
    private String estimateMemoryUsage() {
        long vectorsSize = vectors.size() * 1536 * 4L; // 1536 floats * 4 bytes each
        long contentsSize = contents.values().stream().mapToInt(String::length).sum() * 2L;
        long totalBytes = vectorsSize + contentsSize;
        return String.format("%.2f", totalBytes / (1024.0 * 1024.0));
    }

    /**
     * Export data to human-readable JSON (for debugging)
     */
    public void exportToJson(String filename) throws IOException {
        Map<String, Object> exportData = new HashMap<>();

        // Convert vectors to List<Double>
        Map<String, List<Double>> exportVectors = new HashMap<>();
        for (Map.Entry<String, float[]> entry : vectors.entrySet()) {
            List<Double> doubleList = new ArrayList<>();
            for (float f : entry.getValue()) {
                doubleList.add((double) f);
            }
            exportVectors.put(entry.getKey(), doubleList);
        }

        exportData.put("vectors", exportVectors);
        exportData.put("contents", contents);
        exportData.put("metadata", Map.of(
                "totalEntries", vectors.size(),
                "exportDate", new Date().toString(),
                "embeddingDimensions", vectors.isEmpty() ? 0 : vectors.values().iterator().next().length
        ));

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filename), exportData);

        System.out.println("📤 Exported data to: " + filename);
    }

    /**
     * Backup current data
     */
    public void backup() {
        String backupName = STORAGE_DIR + "_backup_" + System.currentTimeMillis();
        try {
            Path source = Paths.get(STORAGE_DIR);
            Path target = Paths.get(backupName);

            if (Files.exists(source)) {
                Files.walk(source)
                        .forEach(src -> {
                            try {
                                Path dest = target.resolve(source.relativize(src));
                                if (Files.isDirectory(src)) {
                                    Files.createDirectories(dest);
                                } else {
                                    Files.copy(src, dest);
                                }
                            } catch (IOException e) {
                                System.err.println("Backup failed: " + e.getMessage());
                            }
                        });
                System.out.println("Backup created: " + backupName);
            }
        } catch (IOException e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
        }
    }
}