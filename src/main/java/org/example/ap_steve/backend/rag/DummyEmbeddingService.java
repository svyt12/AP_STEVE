package org.example.ap_steve.backend.rag;

import java.util.Random;

public class DummyEmbeddingService implements EmbeddingService {

    @Override
    public float[] embed(String text) {
        float[] vector = new float[384]; // typical embedding size
        Random random = new Random();
        for (int i = 0; i < vector.length; i++) {
            vector[i] = random.nextFloat();
        }
        return vector;
    }
}
