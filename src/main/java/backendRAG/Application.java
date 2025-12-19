package backendRAG;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public VectorStore vectorStore() {
        return new InMemoryVectorStore();
    }

    @Bean
    public EmbeddingService embeddingService() {
        return new DummyEmbeddingService();
    }

    @Bean
    public DocumentLoader documentLoader(VectorStore vectorStore, EmbeddingService embeddingService) {
        return new DocumentLoader(vectorStore, embeddingService);
    }
}