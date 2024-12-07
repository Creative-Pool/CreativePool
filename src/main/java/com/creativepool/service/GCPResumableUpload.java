package com.creativepool.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GCPResumableUpload {
    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;
    public GCPResumableUpload(@Value("${credential.file}") String credentialFile, @Value("${project.id}") String projectId) throws IOException {
        Resource resource = new ClassPathResource(credentialFile);
        Credentials credentials = GoogleCredentials
                .fromStream(resource.getInputStream());
        storage = StorageOptions.newBuilder().setCredentials(credentials)
                .setProjectId(projectId).build().getService();
    }

    public  void resumableUpload() throws IOException, URISyntaxException {
        String bucketName = "creative-pool"; // Replace with your bucket name
        String objectName = "0bece4f7-5af2-49a7-88ad-8dab2fcc3b3d.jpg"; // Replace with how you want the file to be named in the bucket
        String filePath = "C:\\Users\\Nitish\\Downloads\\0bece4f7-5af2-49a7-88ad-8dab2fcc3b3d.jpg"; // Replace with your file path
        // Initialize the Google Cloud Storage client

        // Create metadata for the blob (object) to be uploaded
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();

        // Create the resumable upload session URL
        URL uploadUrl = storage.signUrl(
                blobInfo,
                1,
                TimeUnit.HOURS,
                Storage.SignUrlOption.httpMethod(HttpMethod.POST),
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.withQueryParams(Map.of("uploadType", "resumable")),
                Storage.SignUrlOption.withExtHeaders(Map.of("x-goog-resumable", "start"))

        );

        System.out.println("Resumable upload URL: " + uploadUrl);

        // Open a connection to the resumable upload URL
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type","image/jpeg");
        headers.set("x-goog-resumable", "start");

        // Create an empty request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Use RestTemplate to make the POST request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =restTemplate.exchange(uploadUrl.toURI(), org.springframework.http.HttpMethod.POST, requestEntity, String.class);

//        int responseCode = response.getStatusCode().value();
//        if (responseCode != HttpURLConnection.HTTP_OK) {
//            throw new IOException("Failed to initiate the resumable upload session. Response code: " + responseCode);
//        }

        // Retrieve the resumable upload session URI from the response header
        String uploadSessionUrl = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (uploadSessionUrl == null) {
            throw new IOException("Could not retrieve the resumable upload session URL.");
        }

        System.out.println("Resumable session URL: " + uploadSessionUrl);

        // Now upload the file using the resumable session URL
        uploadFileChunks(uploadSessionUrl, filePath);
    }

    private static void uploadFileChunks(String uploadSessionUrl, String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();

        // Read the file and upload in chunks
        try (FileInputStream inputStream = new FileInputStream(file)) {
            HttpURLConnection uploadConnection = (HttpURLConnection) new URL(uploadSessionUrl).openConnection();
            uploadConnection.setDoOutput(true);
            uploadConnection.setRequestMethod("PUT");
            uploadConnection.setRequestProperty("Content-Type", "image/jpeg");
            uploadConnection.setRequestProperty("Content-Length", String.valueOf(fileSize));

            try (OutputStream outputStream = uploadConnection.getOutputStream()) {
                byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // Complete the upload and check response
            int uploadResponseCode = uploadConnection.getResponseCode();
            if (uploadResponseCode == HttpURLConnection.HTTP_OK
                    || uploadResponseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("File uploaded successfully.");
            } else {
                throw new IOException("File upload failed. Response code: " + uploadResponseCode);
            }
        }
    }
}