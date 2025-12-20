package backend.rag;

import java.util.List;
import java.util.Map;

public interface VectorStore {
    void store(String id, String content, float[] embedding);

    List<SearchResult> searchSimilar(float[] queryEmbedding, int topK);
    Map<String, String> getContents();
    Map<String, float[]> getVectors();
    void clear();

}
