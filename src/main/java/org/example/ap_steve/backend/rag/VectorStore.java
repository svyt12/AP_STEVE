package org.example.ap_steve.backend.rag;

public interface VectorStore {
    void store(String id, String text, float[] embedding);
}
