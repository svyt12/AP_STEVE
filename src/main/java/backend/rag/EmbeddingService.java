package backend.rag;

public interface EmbeddingService {
    float[] embed(String text) throws Exception;
    String getModelName();
}