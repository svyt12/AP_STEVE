package backend.controller;

import backend.rag.DocumentLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:8080") // Adjust port if needed
public class DocumentUploadController {

    private final DocumentLoader documentLoader;

    public DocumentUploadController(DocumentLoader documentLoader) {
        this.documentLoader = documentLoader;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body("Only PDF files are supported");
            }

            // Save file temporarily
            Path tempFile = Files.createTempFile("upload-", ".pdf");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // Process the PDF
            documentLoader.loadPdf(tempFile.toFile());

            // Clean up
            Files.delete(tempFile);

            return ResponseEntity.ok("Document uploaded and processed successfully!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing document: " + e.getMessage());
        }
    }
}