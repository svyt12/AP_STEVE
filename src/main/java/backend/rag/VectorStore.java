package backend.rag;

import java.util.List;
import java.util.Map;

public interface VectorStore {
    void addVector(String documentId, String content, float[] embedding);
    List<SearchResult> searchSimilar(float[] queryEmbedding, int topK);

    // persistence methods
    void saveToFile(String filePath);
    void loadFromFile(String filePath);

    int getVectorCount();
    Map<String, String> getVectors();

    void clear();
}