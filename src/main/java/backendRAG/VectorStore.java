package backendRAG;

public interface VectorStore {
    void store(String id, String content, float[] embedding);
}
