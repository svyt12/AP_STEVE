package backend;

import backend.config.OpenAiProperties;
import backend.config.RagProperties;
import backend.rag.*;
import backend.service.RAGQueryService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({
        RagProperties.class,
        OpenAiProperties.class
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("S.T.E.V.E RAG Backend started on http://localhost:8080");
        System.out.println("Available endpoints:");
        System.out.println("  POST /api/documents/upload - Upload PDF documents");
        System.out.println("  POST /api/chat/ask - Ask questions about documents");
        System.out.println("  GET  /api/chat/health - Health check");
    }

    @Bean
    public VectorStore vectorStore() {
        return new InMemoryVectorStore();
    }

    @Bean
    public EmbeddingService embeddingService() {
        return new OpenAiEmbeddingService();
    }

    @Bean
    public RAGQueryService ragQueryService(VectorStore vectorStore, EmbeddingService embeddingService) {
        System.out.println("Creating RAGQueryService bean");
        return new RAGQueryService(vectorStore, embeddingService);
    }
}