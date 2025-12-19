package backend;

import backend.rag.*;
import backend.service.RAGQueryService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
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
        return new DummyEmbeddingService();
    }

    @Bean
    public DocumentLoader documentLoader(VectorStore vectorStore, EmbeddingService embeddingService) {
        return new DocumentLoader(vectorStore, embeddingService);
    }

    @Bean
    public RAGQueryService ragQueryService(VectorStore vectorStore) {
        return new RAGQueryService((InMemoryVectorStore) vectorStore);
    }
}