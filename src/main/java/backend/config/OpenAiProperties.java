package backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        String apiKey,
        Embedding embedding,
        Chat chat
) {
    public record Embedding(String model) {}
    public record Chat(String model) {}
}
