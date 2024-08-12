package com.creativepool.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.net.MalformedURLException;

@Service
public class CloudStorageService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;


    public CloudStorageService(@Value("${credential.file}") String credentialFile, @Value("${project.id}") String projectId) throws IOException {
        Resource resource = new ClassPathResource(credentialFile);
        Credentials credentials = GoogleCredentials
                .fromStream(resource.getInputStream());
        storage = StorageOptions.newBuilder().setCredentials(credentials)
                .setProjectId(projectId).build().getService();
    }


    public void uploadFile(MultipartFile file, List<String> filenames) throws IOException {
        String blobName = UUID.randomUUID().toString();
        BlobInfo blobInfo = storage.create(
                BlobInfo.newBuilder(bucketName, blobName).setContentType(file.getContentType()).build(),
                file.getBytes()
        );
        URL url =
                storage.signUrl(
                        blobInfo,
                        6,
                        TimeUnit.DAYS,
                        Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                        Storage.SignUrlOption.withV4Signature());
        filenames.add(getFilenameFromSignedUrl(url.getFile()));
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

}
