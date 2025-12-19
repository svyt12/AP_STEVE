package backendRAG;

import java.util.HashMap;
import java.util.Map;

public class InMemoryVectorStore implements VectorStore {

    private final Map<String, float[]> vectors = new HashMap<>();

    @Override
    public void store(String id, String content, float[] embedding) {
        vectors.put(id, embedding);
        System.out.println("Stored document: " + id);
    }
}
