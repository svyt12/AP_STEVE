package frontend.services;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

public class FileUploadService {

    // ✅ MATCH UploadDocument
    private static final String UPLOAD_URL =
            "http://localhost:8080/api/documents/upload";

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

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String boundary = "----JavaFXBoundary" + System.currentTimeMillis();

        byte[] requestBody = createMultipartBody(file, fileBytes, boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPLOAD_URL))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return "Upload successful";
        }

        throw new RuntimeException(
                "Upload failed (" + response.statusCode() + "): " + response.body()
        );
    }

    private byte[] createMultipartBody(File file, byte[] fileBytes, String boundary) {

        String header =
                "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" +
                        file.getName() + "\"\r\n" +
                        "Content-Type: application/pdf\r\n\r\n";

        String footer = "\r\n--" + boundary + "--\r\n";

        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] footerBytes = footer.getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[
                headerBytes.length + fileBytes.length + footerBytes.length
                ];

        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, body,
                headerBytes.length + fileBytes.length,
                footerBytes.length);

        return body;
    }
}
