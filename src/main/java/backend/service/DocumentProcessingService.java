package backend.service;

import backend.rag.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.*;

@Service
public class DocumentProcessingService {

    @Autowired
    private PdfParserService pdfParser;

    @Autowired
    private TextChunkerService chunker;

    @Autowired
    private OpenAiEmbeddingService embeddingService;

    @Autowired
    private InMemoryVectorStore vectorStore;

    public String processPdfDocument(File pdfFile, String documentId) {
        try {
            System.out.println("📄 Processing PDF: " + pdfFile.getName());

            // 1. Extract text from PDF
            String fullText = pdfParser.extractTextFromPdf(pdfFile);
            System.out.println("   Extracted " + fullText.length() + " characters");

            // 2. Split into chunks
            List<String> chunks = chunker.chunkText(fullText, 1000, 100);
            System.out.println("   Split into " + chunks.size() + " chunks");

            // 3. Create embeddings for each chunk
            for (int i = 0; i < chunks.size(); i++) {
                String chunkId = documentId + "_chunk_" + i;
                String chunkText = chunks.get(i);

                System.out.println("   Creating embedding for chunk " + i + " (" + chunkText.length() + " chars)");

                // 4. Generate embedding
                float[] embedding = embeddingService.embed(chunkText);

                // 5. Store in vector database
                vectorStore.store(chunkId, chunkText, embedding);

                System.out.println("   ✓ Stored chunk " + i);
            }

            return "Successfully processed PDF with " + chunks.size() + " chunks";

        } catch (Exception e) {
            throw new RuntimeException("Failed to process PDF: " + e.getMessage(), e);
        }
    }
}