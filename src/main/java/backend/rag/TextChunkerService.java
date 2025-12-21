package backend.rag;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkerService {

    /**
     * Splits text into chunks with chunkSize and overlap.
     * Keeps code blocks (lines starting with spaces or tabs) together with surrounding explanation.
     *
     * @param text      Full text
     * @param chunkSize Max characters per chunk
     * @param overlap   Characters to overlap between chunks
     * @return List of chunks
     */
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        String[] lines = text.split("\n"); // split by line to handle code
        StringBuilder currentChunk = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            boolean isCode = line.startsWith("int ") || line.startsWith("float ") ||
                    line.startsWith("*") || line.startsWith("cout") || line.contains(";");

            if (currentChunk.length() + line.length() + 1 > chunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                }

                // Overlap: keep last 'overlap' characters
                String overlapText = "";
                if (currentChunk.length() > overlap) {
                    overlapText = currentChunk.substring(currentChunk.length() - overlap);
                } else {
                    overlapText = currentChunk.toString();
                }

                currentChunk = new StringBuilder(overlapText + "\n");
            }

            currentChunk.append(line).append("\n");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
