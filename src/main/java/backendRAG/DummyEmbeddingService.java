package backendRAG;

public class DummyEmbeddingService implements EmbeddingService {
    @Override
    public float[] embed(String text) {
        // Create a simple dummy embedding (384-dimension like MiniLM)
        float[] embedding = new float[384];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = (float) (Math.random() * 2 - 1); // Random values between -1 and 1
        }
        return embedding;
    }
}