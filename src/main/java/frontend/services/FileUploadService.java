package frontend.services;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;

public class FileUploadService {

    private static final String UPLOAD_URL = "http://localhost:8080/api/documents/upload";

    public String uploadDocument(File file) {
        try {
            // Validate file type
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("Only PDF files are supported");
            }

            RestTemplate restTemplate = new RestTemplate();

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));
            body.add("documentId", "doc_" + System.currentTimeMillis());

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // Send request
            ResponseEntity<String> response = restTemplate.exchange(
                    UPLOAD_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }
}