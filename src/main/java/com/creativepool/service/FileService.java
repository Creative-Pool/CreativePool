package com.creativepool.service;


import com.creativepool.entity.TicketResult;
import com.creativepool.repository.TicketResultRepository;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {

    private final Storage storage;

//    private final Path fileStorageLocation = Paths.get("file_storage").toAbsolutePath().normalize();


    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;


    @Autowired
    TicketResultRepository ticketResultRepository;

    public FileService(@Value("${credential.file}") String credentialFile, @Value("${project.id}") String projectId) throws IOException {
        Resource resource = new ClassPathResource(credentialFile);
        Credentials credentials = GoogleCredentials
                .fromStream(resource.getInputStream());
        storage = StorageOptions.newBuilder().setCredentials(credentials)
                .setProjectId(projectId).build().getService();
    }

    public void uploadFile(MultipartFile file, UUID ticketId) throws IOException {
        String blobName = file.getOriginalFilename();
        BlobInfo blobInfo = storage.create(
                BlobInfo.newBuilder(bucketName, blobName).build(),
                file.getBytes()
        );
        URL url =
                storage.signUrl(
                        blobInfo,
                        15,
                        TimeUnit.MINUTES,
                        Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                        Storage.SignUrlOption.withV4Signature());
        url.toString();
        TicketResult ticketResult = new TicketResult();
        ticketResult.setTicketId(ticketId);
        ticketResult.setVideoURL(url.toString());
        ticketResultRepository.saveAndFlush(ticketResult);


    }

    public Resource loadFileAsResource(UUID resultId) {
        try {

            Optional<TicketResult> ticketResult = ticketResultRepository.findById(resultId);

            //  Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(ticketResult.get().getVideoURL());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found ");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found", ex);
        }
    }
}