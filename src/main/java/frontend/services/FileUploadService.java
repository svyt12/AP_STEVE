package frontend.services;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

public class FileUploadService {
    private static final String UPLOAD_URL = "http://localhost:8080/api/documents/upload";
    private final HttpClient client;

    public FileUploadService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String uploadDocument(File file) throws Exception {
        if (!file.exists() || !file.getName().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Invalid PDF file");
        }

        // Read file as bytes
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Create boundary
        String boundary = "----JavaFXBoundary" + System.currentTimeMillis();

        // Build multipart request body
        byte[] requestBody = createMultipartBody(file, fileBytes, boundary);

        // Create request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPLOAD_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        // Send request
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return "Success: " + response.body();
        } else {
            throw new RuntimeException("Upload failed: " + response.statusCode()
                    + " - " + response.body());
        }
    }

    private byte[] createMultipartBody(File file, byte[] fileBytes, String boundary) {
        String fileName = file.getName();
        String partHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: application/pdf\r\n\r\n";

        String partFooter = "\r\n--" + boundary + "--\r\n";

        // Combine parts
        byte[] headerBytes = partHeader.getBytes();
        byte[] footerBytes = partFooter.getBytes();

        byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];

        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length,
                footerBytes.length);

        return body;
    }
}