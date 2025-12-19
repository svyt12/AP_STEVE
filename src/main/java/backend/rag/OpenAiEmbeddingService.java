package backend.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class OpenAiEmbeddingService implements EmbeddingService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.embedding.model:text-embedding-3-small}")
    private String embeddingModel;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiEmbeddingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public float[] embed(String text) throws Exception {
        // ========== SAFETY CHECK ==========
        if (apiKey == null || apiKey.isEmpty() ||
                apiKey.contains("REPLACE") ||
                apiKey.contains("YOUR_KEY") ||
                apiKey.startsWith("sk-example") ||
                apiKey.equals("your-actual-key-here")) {

            System.out.println("⚠️ WARNING: Using placeholder/dummy API key");
            System.out.println("To use real OpenAI API:");
            System.out.println("1. Get key from: https://platform.openai.com/api-keys");
            System.out.println("2. Add to: src/main/resources/application.properties");
            System.out.println("3. Add: openai.api.key=your-real-key-here");

            // Return dummy embedding for testing
            return createDummyEmbedding(text);
        }

        // ========== REAL OPENAI API CALL ==========
        // Clean and prepare text
        String cleanedText = cleanTextForEmbedding(text);

        // Prepare JSON request
        String requestBody = String.format(
                "{\"model\": \"%s\", \"input\": \"%s\"}",
                embeddingModel,
                cleanedText.replace("\"", "\\\"")
        );

        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/embeddings"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("User-Agent", "Java-RAG-App")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        // Send request
        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        // Check response
        if (response.statusCode() == 401) {
            throw new RuntimeException("Invalid OpenAI API key. Please check your key.");
        } else if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "OpenAI API error: " + response.statusCode() +
                            " - " + response.body()
            );
        }

        // Parse response
        String responseBody = response.body();
        OpenAiEmbeddingResponse embeddingResponse = objectMapper.readValue(
                responseBody,
                OpenAiEmbeddingResponse.class
        );

        // Convert List<Double> to float[]
        List<Double> embeddingList = embeddingResponse.data.get(0).embedding;
        float[] embedding = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            embedding[i] = embeddingList.get(i).floatValue();
        }

        System.out.println("✓ Created real OpenAI embedding with " + embedding.length + " dimensions");
        return embedding;
    }

    private float[] createDummyEmbedding(String text) {
        System.out.println("Creating dummy embedding for text length: " + text.length());

        // Create realistic-looking dummy embedding (same size as OpenAI's)
        float[] embedding = new float[1536]; // text-embedding-3-small has 1536 dimensions
        for (int i = 0; i < embedding.length; i++) {
            // More realistic distribution (centered around 0, range -1 to 1)
            embedding[i] = (float) (Math.random() * 2 - 1);
        }

        // Make first few values deterministic based on text length
        if (text.length() > 0) {
            embedding[0] = (text.length() % 100) / 100.0f;
            embedding[1] = text.split(" ").length / 100.0f;
        }

        return embedding;
    }

    private String cleanTextForEmbedding(String text) {
        if (text == null) return "";

        // Remove excessive whitespace
        text = text.replaceAll("\\s+", " ").trim();

        // Truncate if too long (OpenAI has limits)
        int maxLength = 8000;
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength);
            System.out.println("⚠️ Text truncated to " + maxLength + " characters");
        }

        return text;
    }

    @Override
    public String getModelName() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("REPLACE")) {
            return "dummy-model-for-testing";
        }
        return embeddingModel;
    }

    // Simple DTO classes for JSON parsing
    private static class OpenAiEmbeddingResponse {
        public List<EmbeddingData> data;
    }

    private static class EmbeddingData {
        public List<Double> embedding;
    }
}