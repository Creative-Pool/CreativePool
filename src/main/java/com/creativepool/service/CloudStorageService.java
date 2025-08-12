package com.creativepool.service;

import com.creativepool.exception.CreativePoolException;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.net.MalformedURLException;



@Service
public class CloudStorageService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Autowired
    RestTemplate restTemplate;

    Logger logger= LoggerFactory.getLogger(CloudStorageService.class);

    public CloudStorageService(@Value("${credential.file}") String credentialFile, @Value("${project.id}") String projectId) throws IOException {
        InputStream serviceAccountStream;
        try {
            serviceAccountStream = getClass().getClassLoader().getResourceAsStream(credentialFile);
        }
        catch (Exception e) {
            String json = System.getenv(credentialFile);
            serviceAccountStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }
        Credentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
        storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .getService();
    }


    public void uploadFile(MultipartFile file, List<String> filenames) throws IOException {
        try {
            String blobName = UUID.randomUUID().toString();
            BlobInfo blobInfo = storage.create(BlobInfo.newBuilder(bucketName, blobName).setContentType(file.getContentType()).build(), file.getBytes());
            URL url = storage.signUrl(blobInfo, 6, TimeUnit.DAYS, Storage.SignUrlOption.httpMethod(HttpMethod.GET), Storage.SignUrlOption.withV4Signature());
            filenames.add(getFilenameFromSignedUrl(url.getFile()));
            logger.info("File uploaded successfully: {}", blobName);
        } catch (Exception e) {
            logger.error("Failed to upload file", e);
            throw new CreativePoolException("Error uploading file");
        }
    }

    public void deleteFile(String bucketName, String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        boolean deleted = storage.delete(blobId);
    }

    public void deleteFileUsingSignedUrl(String signedUrl) throws MalformedURLException {
        String fileName = getFilenameFromSignedUrl(signedUrl);
        deleteFile(bucketName, fileName);
    }

    public  String getFilenameFromSignedUrl(String signedUrl) throws MalformedURLException {

        String[] parts = signedUrl.split("/");
        String encodedFileName = parts[parts.length - 1].split("\\?")[0];

        // Decode the file name
        return URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);

    }

    public String generateSignedUrl(String fileName) throws IOException {
        if(!StringUtils.isEmpty(fileName)) {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
            URL signedUrl = storage.signUrl(
                    blobInfo,
                    6,
                    TimeUnit.DAYS,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                    Storage.SignUrlOption.withV4Signature()
            );
            return signedUrl.toString();
        }
        return null;
    }

    public Blob getBlob(String filename){
        return storage.get(BlobId.of(bucketName, filename));

    }


    public String generateSignedUrlForUpload(String objectName){

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();

        // Define the expiration time for the signed URL
        long expiration = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1); // 1 hour

        // Generate signed URL
        URL signedUrl = storage.signUrl(
                blobInfo,
                1,
                TimeUnit.HOURS,
                Storage.SignUrlOption.httpMethod(HttpMethod.POST),
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.withQueryParams(Map.of("uploadType", "resumable")),
                Storage.SignUrlOption.withExtHeaders(Map.of("x-goog-resumable", "start"))

        );

        System.out.println("Signed URL: " + signedUrl.toString());



        return signedUrl.toString();
    }

    public String resumableUpload(String filename,String fileType) throws URISyntaxException {

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filename).build();
        // Create the resumable upload session URL
        URL uploadUrl = storage.signUrl(blobInfo, 1, TimeUnit.HOURS, Storage.SignUrlOption.httpMethod(HttpMethod.POST), Storage.SignUrlOption.withV4Signature(), Storage.SignUrlOption.withQueryParams(Map.of("uploadType", "resumable")), Storage.SignUrlOption.withExtHeaders(Map.of("x-goog-resumable", "start")));

        // Open a connection to the resumable upload URL
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", fileType);
        headers.set("x-goog-resumable", "start");

        // Create an empty request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Use RestTemplate to make the POST request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uploadUrl.toURI(), org.springframework.http.HttpMethod.POST, requestEntity, String.class);


        // Retrieve the resumable upload session URI from the response header
        String uploadSessionUrl = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (uploadSessionUrl == null) {
            throw new CreativePoolException("Could not retrieve the resumable upload session URL.");
        }

        return uploadSessionUrl;
    }


}
