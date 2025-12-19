package backend.rag;

import java.util.List;
import java.util.Map;

public interface VectorStore {
    void store(String id, String content, float[] embedding);

    // ADD THESE METHODS:
    List<SearchResult> searchSimilar(float[] queryEmbedding, int topK);
    Map<String, String> getContents();
    Map<String, float[]> getVectors();
    void clear();

    // Add this inner class for search results
    class SearchResult {
        public final String id;
        public final String content;
        public final float similarity;

        public SearchResult(String id, String content, float similarity) {
            this.id = id;
            this.content = content;
            this.similarity = similarity;
        }
    }
}
