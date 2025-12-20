package backend.service;

import backend.rag.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class DocumentUploadService {

    @Autowired
    private OpenAiEmbeddingService embeddingService;

    @Autowired
    private InMemoryVectorStore vectorStore;

    @Autowired
    private DocumentStorageService documentStorageService;

    public UploadResult processUpload(MultipartFile file, String documentId) throws Exception {
        UploadResult result = new UploadResult();

        try {
            System.out.println("📄 Processing upload: " + file.getOriginalFilename());

            String savedPath = documentStorageService.saveDocument(file, documentId);
            result.setFilePath(savedPath);

            String placeholderText = "Document: " + file.getOriginalFilename() +
                    "\nUploaded at: " + new Date() +
                    "\nSize: " + file.getSize() + " bytes";

            float[] embedding = embeddingService.embed(placeholderText);
            vectorStore.addVector(documentId, placeholderText, embedding);

            result.setSuccess(true);
            result.setMessage("Document uploaded successfully");
            result.setProcessedChunks(1);
            result.setTotalChunks(1);

            System.out.println("✅ Upload completed: " + result.getMessage());

        } catch (Exception e) {
            System.err.println("❌ Upload processing failed: " + e.getMessage());
            result.setSuccess(false);
            result.setMessage("Upload processing failed: " + e.getMessage());
            throw e;
        }

        return result;
    }

    public int getTotalDocuments() {
        return documentStorageService.getAllStoredDocuments().size();
    }

    public int getTotalVectors() {
        return vectorStore.getVectorCount();
    }

    // DTO for upload results
    public static class UploadResult {
        private boolean success;
        private String message;
        private String filePath;
        private int processedChunks;
        private int totalChunks;

        // Getters and setters
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