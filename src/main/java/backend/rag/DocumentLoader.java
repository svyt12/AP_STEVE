package backend.rag;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.util.List;

@Service
public class DocumentLoader {

    @Autowired
    private PdfParserService pdfParser;

    @Autowired
    private TextChunkerService chunker;

    @Autowired
    private OpenAiEmbeddingService embeddingService;

    @Autowired
    private InMemoryVectorStore vectorStore;

    public void loadPdf(File pdfFile) {
        try {
            System.out.println("\n📄 ======= Processing PDF: " + pdfFile.getName() + " =======");

            // 1. Extract text
            System.out.println("1. Extracting text from PDF...");
            String text = pdfParser.extractTextFromPdf(pdfFile);
            System.out.println("   ✓ Extracted " + text.length() + " characters");

            // 2. Split into chunks
            System.out.println("2. Splitting text into chunks...");
            List<String> chunks = chunker.chunkText(text, 1000, 100);
            System.out.println("   ✓ Created " + chunks.size() + " chunks");

            // 3. Create and store embeddings
            System.out.println("3. Creating embeddings...");
            for (int i = 0; i < chunks.size(); i++) {
                String chunkId = pdfFile.getName() + "_chunk_" + i;
                String chunkText = chunks.get(i);

                System.out.println("   [" + (i+1) + "/" + chunks.size() + "] Creating embedding...");
                float[] embedding = embeddingService.embed(chunkText);

                vectorStore.store(chunkId, chunkText, embedding);
                System.out.println("   ✓ Stored chunk " + (i+1));
            }

            System.out.println("✅ PDF processing complete! Total chunks: " + chunks.size() + "\n");

        } catch (Exception e) {
            System.err.println("❌ PDF processing failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load PDF", e);
        }
    }
}