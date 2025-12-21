package backend.service;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.Collections;
// wrapper for GPT API
@Service
public class GPTService {

    private final OpenAiService openAi;

    public GPTService() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        this.openAi = new OpenAiService(apiKey);
    }

    /**
     * Simple wrapper to call GPT chat completion
     */
    public String chatCompletion(String prompt) {
        ChatMessage message = new ChatMessage("user", prompt);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(Collections.singletonList(message))
                .temperature(0.0)
                .maxTokens(500)
                .build();

        return openAi.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .trim();
    }
}
