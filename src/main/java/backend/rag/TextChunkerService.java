package backend.rag;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkerService {

    /**
     * Splits text into chunks with given size and overlap.
     * @param text The full text to split
     * @param chunkSize Max number of characters per chunk
     * @param overlap Number of characters to repeat from previous chunk
     * @return List of text chunks
     */
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        String[] sentences = text.split("(?<=[.!?])\\s+"); // split by sentence
        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > chunkSize) {
                // Add current chunk
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                }

                // Keep last 'overlap' characters for next chunk
                String overlapText = "";
                if (currentChunk.length() > overlap) {
                    overlapText = currentChunk.substring(currentChunk.length() - overlap);
                } else {
                    overlapText = currentChunk.toString();
                }
                currentChunk = new StringBuilder(overlapText + " ");
            }
            currentChunk.append(sentence).append(" ");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
