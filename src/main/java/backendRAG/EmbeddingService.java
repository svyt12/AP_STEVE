package backendRAG;

public interface EmbeddingService {
    float[] embed(String text);
}
