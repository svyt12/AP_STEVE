package backend.controller;

import backend.service.DocumentUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentUploadController {

    @Autowired
    private DocumentUploadService uploadService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
                response.put("success", false);
                response.put("message", "Only PDF files are supported");
                return ResponseEntity.badRequest().body(response);
            }

            // Generate a document ID
            String documentId = "doc_" + System.currentTimeMillis() + "_" +
                    originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");

            System.out.println("📤 Receiving upload: " + originalFilename + " (" + file.getSize() + " bytes)");

            // Process the upload
            DocumentUploadService.UploadResult result = uploadService.processUpload(file, documentId);

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("documentId", documentId);
            response.put("filename", originalFilename);
            response.put("fileSize", file.getSize());
            response.put("chunksProcessed", result.getProcessedChunks());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Upload failed: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Document Upload Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDocuments", uploadService.getTotalDocuments());
            stats.put("totalVectors", uploadService.getTotalVectors());
            stats.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}