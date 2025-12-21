package backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentStorageService {

    private final List<String> storedDocuments = new ArrayList<>();

    public String saveDocument(MultipartFile file, String documentId) {
        storedDocuments.add(documentId);
        // TODO: actually save file to disk if needed
        return "storage/" + file.getOriginalFilename();
    }

    public int getStoredDocumentCount() {
        return storedDocuments.size();
    }
}
