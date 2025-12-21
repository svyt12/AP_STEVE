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
import java.util.Map;
import java.util.HashMap;

@Service
public class OpenAiEmbeddingService implements EmbeddingService {

    @Value("${openai.api.key:}")
    private String apiKeyFromProperty;

    @Value("${openai.embedding.model:text-embedding-3-small}")  // semantic matching
    private String embeddingModel;

    private String apiKey = null;
    private boolean initialized = false;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiEmbeddingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // Initialize on first use (lazy initialization)
    private void initializeIfNeeded() {
        if (initialized) return;

        System.out.println("\n🔑 Initializing OpenAI Embedding Service...");

        // 1. Try application.properties
        if (apiKeyFromProperty != null && !apiKeyFromProperty.trim().isEmpty()) {
            apiKey = apiKeyFromProperty.trim();
            System.out.println("   Using key from application.properties");
        }

        // 2. Try environment variable
        if (apiKey == null || apiKey.isEmpty()) {
            String envKey = System.getenv("OPENAI_API_KEY");
            if (envKey != null && !envKey.trim().isEmpty()) {
                apiKey = envKey.trim();
                System.out.println("   Using key from environment variable");
            }
        }

        // 3. Validate the key
        if (apiKey == null || apiKey.isEmpty() || !isValidApiKey(apiKey)) {
            System.out.println("⚠️ No valid API key found - using dummy embeddings");
            System.out.println("   Set OPENAI_API_KEY environment variable or openai.api.key in application.properties");
            apiKey = null; // Use dummy mode
        } else {
            String maskedKey = maskApiKey(apiKey);
            System.out.println("✅ OpenAI API Key loaded: " + maskedKey);
            System.out.println("   Model: " + embeddingModel);
        }

        initialized = true;
        System.out.println("=== Initialization Complete ===\n");
    }

    private boolean isValidApiKey(String key) {
        if (key == null || key.trim().isEmpty()) return false;
        key = key.trim();

        // Reject placeholder keys
        if (key.contains("REPLACE") ||
                key.contains("YOUR_KEY") ||
                key.startsWith("sk-example") ||
                key.equals("your-actual-key-here") ||
                key.equals("sk-your-key-here")) {
            return false;
        }

        // Accept real keys
        return key.startsWith("sk-") && key.length() > 20;
    }

    private String maskApiKey(String key) {
        if (key == null || key.length() < 12) return "***";
        return key.substring(0, 8) + "..." + key.substring(key.length() - 4);
    }

    @Override
    public float[] embed(String text) throws Exception {
        // Initialize on first use
        initializeIfNeeded();

        System.out.println("\n🔧 Creating embedding for text (" + text.length() + " chars)");

        // Check if we have a real API key
        if (apiKey == null) {
            System.out.println("⚠️ No valid API key - using dummy embedding");
            return createDummyEmbedding(text);
        }

        try {
            String cleanedText = cleanTextForEmbedding(text);

            // JSON request
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", embeddingModel);
            requestMap.put("input", cleanedText);
            requestMap.put("encoding_format", "float");

            String requestBody = objectMapper.writeValueAsString(requestMap);

            System.out.println("📤 Calling OpenAI API with model: " + embeddingModel);

            //HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/embeddings"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "AP-STEVE-RAG-App/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("OpenAI Response Status: " + response.statusCode());

            if (response.statusCode() == 401) {
                System.err.println("Invalid OpenAI API key (401 Unauthorized)");
                throw new RuntimeException("Invalid OpenAI API key. Please check your key.");
            } else if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "OpenAI API error: " + response.statusCode() +
                                " - " + response.body()
                );
            }

            String responseBody = response.body();
            Map<String, Object> responseMap = objectMapper.readValue(
                    responseBody, Map.class);

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            if (data == null || data.isEmpty()) {
                throw new RuntimeException("No embedding data in OpenAI response");
            }

            List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");
            if (embeddingList == null || embeddingList.isEmpty()) {
                throw new RuntimeException("Empty embedding in response");
            }

            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }

            System.out.println("Created real OpenAI embedding with " + embedding.length + " dimensions");
            return embedding;

        } catch (Exception e) {
            System.err.println("OpenAI API call failed: " + e.getMessage());
            System.err.println("⚠Falling back to dummy embedding");
            return createDummyEmbedding(text);
        }
    }

    private float[] createDummyEmbedding(String text) {
        System.out.println("🔄 Creating dummy embedding for testing");

        // Create realistic dummy embedding
        int dimensions = embeddingModel.contains("large") ? 3072 : 1536;
        float[] embedding = new float[dimensions];

        int textHash = text != null ? Math.abs(text.hashCode()) : 0;

        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = (float) Math.sin(textHash * (i + 1) * 0.01) * 0.5f;
        }

        // Normalize
        float sumSq = 0;
        for (float v : embedding) {
            sumSq += v * v;
        }
        float norm = (float) Math.sqrt(sumSq);
        if (norm > 0) {
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= norm;
            }
        }

        System.out.println("✅ Created dummy embedding with " + embedding.length + " dimensions");
        return embedding;
    }

    private String cleanTextForEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "empty";
        }

        text = text.replaceAll("\\s+", " ").trim();

        int maxLength = 6000;
        if (text.length() > maxLength) {
            System.out.println("⚠Text truncated from " + text.length() + " to " + maxLength + " characters");
            text = text.substring(0, maxLength);
        }

        return text;
    }
}