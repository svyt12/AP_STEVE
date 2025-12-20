package backend.rag;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkerService {
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        // Simple splitting by sentence
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > chunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    // Keep last few sentences for overlap
                    String[] chunkSentences = currentChunk.toString().split("(?<=[.!?])\\s+");
                    int keep = Math.min(2, chunkSentences.length); // Keep last 2 sentences
                    currentChunk = new StringBuilder();
                    for (int i = chunkSentences.length - keep; i < chunkSentences.length; i++) {
                        currentChunk.append(chunkSentences[i]).append(" ");
                    }
                }
            }
            currentChunk.append(sentence).append(" ");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}