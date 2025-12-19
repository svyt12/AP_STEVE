package backendRAG;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.util.UUID;

public class DocumentLoader {

    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;

    public DocumentLoader(VectorStore vectorStore, EmbeddingService embeddingService) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
    }

    public void loadPdf(File pdfFile) throws Exception {
        PDDocument document = PDDocument.load(pdfFile);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        float[] embedding = embeddingService.embed(text);
        vectorStore.store(UUID.randomUUID().toString(), text, embedding);
    }
}
