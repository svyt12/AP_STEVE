package test;
import backend.rag.OpenAiEmbeddingService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TestConfig {
    @Bean
    public OpenAiEmbeddingService embeddingService() {
        return new OpenAiEmbeddingService();
    }
}

public class TestOpenAIKey {
    public static void main(String[] args) {
        System.out.println("=== Testing OpenAI Configuration ===\n");

        try {
            // Create Spring context
            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(TestConfig.class);

            // Get the service
            OpenAiEmbeddingService service = context.getBean(OpenAiEmbeddingService.class);

            // Test with a small text
            String testText = "Testing OpenAI connection";

            System.out.println("Attempting to call OpenAI API...");
            float[] embedding = service.embed(testText);

            System.out.println("\n✅ SUCCESS! OpenAI API key is working!");
            System.out.println("Embedding dimensions: " + embedding.length);
            System.out.println("First 5 values: ");
            for (int i = 0; i < Math.min(5, embedding.length); i++) {
                System.out.printf("  [%d]: %.6f\n", i, embedding[i]);
            }

            context.close();

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());

            // Helpful error messages
            if (e.getMessage().contains("API key") || e.getMessage().contains("not configured")) {
                System.out.println("\n💡 HOW TO FIX:");
                System.out.println("1. Get your API key from: https://platform.openai.com/api-keys");
                System.out.println("2. Create file: src/main/resources/application.properties");
                System.out.println("3. Add this line: openai.api.key=your-key-here");
                System.out.println("4. Make sure the key starts with 'sk-'");
            } else if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                System.out.println("\n💡 Your API key is invalid or expired.");
                System.out.println("Generate a new key at: https://platform.openai.com/api-keys");
            }
        }
    }
}