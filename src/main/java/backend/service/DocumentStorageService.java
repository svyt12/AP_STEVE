package backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class DocumentStorageService {

    @Value("${document.storage.path:./data/uploaded-documents}")
    private String storagePath;

    private final Set<String> processedDocuments = new HashSet<>();

    @PostConstruct
    public void init() {
        createStorageDirectory();
        loadProcessedDocuments();
    }

    private void createStorageDirectory() {
        try {
            Files.createDirectories(Paths.get(storagePath));
            System.out.println("📁 Document storage directory: " + Paths.get(storagePath).toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    public String saveDocument(MultipartFile file, String documentId) throws IOException {

        // Ensure storage directory exists
        Path storageDir = Paths.get(storagePath).toAbsolutePath();
        Files.createDirectories(storageDir);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = documentId + extension;

        Path filePath = storageDir.resolve(filename);

        // Save file
        file.transferTo(filePath.toFile());

        markAsProcessed(documentId);

        System.out.println("✅ Document saved at: " + filePath);
        return filePath.toString();
    }


    public List<String> getAllStoredDocuments() {
        List<String> documents = new ArrayList<>();
        File dir = new File(storagePath);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) ->
                    name.endsWith(".pdf") || name.endsWith(".txt") ||
                            name.endsWith(".doc") || name.endsWith(".docx"));

            if (files != null) {
                for (File file : files) {
                    documents.add(file.getName());
                }
            }
        }

        return documents;
    }

    public File getDocument(String filename) {
        return new File(storagePath, filename);
    }

    public boolean documentExists(String documentId) {
        return processedDocuments.contains(documentId);
    }

    public void markAsProcessed(String documentId) {
        processedDocuments.add(documentId);
        saveProcessedDocuments();
    }

    private void loadProcessedDocuments() {
        File processedFile = new File(storagePath, ".processed");
        if (processedFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(processedFile.toPath());
                processedDocuments.addAll(lines);
                System.out.println("Loaded " + processedDocuments.size() + " processed documents");
            } catch (IOException e) {
                System.err.println("❌ Failed to load processed documents: " + e.getMessage());
            }
        }
    }

    private void saveProcessedDocuments() {
        try {
            Files.write(Paths.get(storagePath, ".processed"),
                    processedDocuments,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("❌ Failed to save processed documents: " + e.getMessage());
        }
    }

    public String getStoragePath() {
        return storagePath;
    }
}