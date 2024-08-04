package com.creativepool.service;

import com.creativepool.entity.TicketResult;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.*;

import java.net.MalformedURLException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class UploadService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;


    public UploadService(@Value("${credential.file}") String credentialFile,@Value("${project.id}") String projectId) throws IOException {
        Resource resource = new ClassPathResource(credentialFile);
        Credentials credentials = GoogleCredentials
                .fromStream(resource.getInputStream());
        storage = StorageOptions.newBuilder().setCredentials(credentials)
                .setProjectId(projectId).build().getService();
    }


    public void uploadFile(MultipartFile file, List<String> uploadedUrls) throws IOException {
        String blobName = file.getOriginalFilename();
        BlobInfo blobInfo = storage.create(
                BlobInfo.newBuilder(bucketName, blobName).build(),
                file.getBytes()
        );
        URL url =
                storage.signUrl(
                        blobInfo,
                        6,
                        TimeUnit.DAYS,
                        Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                        Storage.SignUrlOption.withV4Signature());
        ;
        String filename= url.getFile();
        System.out.println("Hello"+ filename);
        uploadedUrls.add(url.toString());
    }

    public void deleteFile(String bucketName, String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        boolean deleted = storage.delete(blobId);

        if (deleted) {
            System.out.println("File deleted successfully.");
        } else {
            System.out.println("File not found.");
        }
    }

    public void deleteFileUsingSignedUrl(String signedUrl) throws MalformedURLException {
        String[] parsedUrl =parseSignedUrl(signedUrl);
        String bucketName = parsedUrl[0];
        String fileName = parsedUrl[1];
        deleteFile(bucketName, fileName);
    }

    public  String[] parseSignedUrl(String signedUrl) throws MalformedURLException {

        String[] parts = signedUrl.split("/");
        String bucketName = parts[3];
        String encodedFileName = parts[4].split("\\?")[0];

        // Decode the file name
        String decodedFileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);

        return new String[]{bucketName, decodedFileName};

    }

}
