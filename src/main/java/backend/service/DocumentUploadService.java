package backend.service;

import backend.rag.EmbeddingService;
import backend.rag.VectorStore;
import backend.rag.TextChunkerService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentUploadService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final DocumentStorageService documentStorageService;
    private final PdfTextExtractor pdfTextExtractor;
    private final TextChunkerService textChunkerService;

    public DocumentUploadService(
            EmbeddingService embeddingService,
            VectorStore vectorStore,
            DocumentStorageService documentStorageService,
            PdfTextExtractor pdfTextExtractor,
            TextChunkerService textChunkerService
    ) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.documentStorageService = documentStorageService;
        this.pdfTextExtractor = pdfTextExtractor;
        this.textChunkerService = textChunkerService;
    }

    public UploadResult processUpload(MultipartFile file, String documentId) throws Exception {
        UploadResult result = new UploadResult();

        System.out.println("📄 Processing upload: " + file.getOriginalFilename());

        // Save PDF
        String savedPath = documentStorageService.saveDocument(file, documentId);
        result.setFilePath(savedPath);

        // Extract text
        String text = pdfTextExtractor.extractText(file);

        // Chunk text
        List<String> chunks = textChunkerService.chunkText(text, 500, 100);

        int chunkCount = 0;
        for (String chunk : chunks) {
            String chunkId = documentId + "_chunk_" + chunkCount++;
            float[] embedding = embeddingService.embed(chunk);
            vectorStore.addVector(chunkId, chunk, embedding);
        }

        result.setSuccess(true);
        result.setMessage("Document uploaded successfully");
        result.setProcessedChunks(chunkCount);
        result.setTotalChunks(chunkCount);

        System.out.println("🧩 Embedded " + chunkCount + " chunks");

        return result;
    }

    // ================= STATS METHODS =================

    public int getTotalDocuments() {
        return documentStorageService.getStoredDocumentCount();
    }

    public int getTotalVectors() {
        return vectorStore.getVectorCount();
    }

    // ================= DTO =================

    public static class UploadResult {
        private boolean success;
        private String message;
        private String filePath;
        private int processedChunks;
        private int totalChunks;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public int getProcessedChunks() { return processedChunks; }
        public void setProcessedChunks(int processedChunks) { this.processedChunks = processedChunks; }

        public int getTotalChunks() { return totalChunks; }
        public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }
    }
}
