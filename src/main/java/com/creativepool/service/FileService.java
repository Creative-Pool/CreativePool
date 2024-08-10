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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FileService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Autowired
    TicketResultRepository ticketResultRepository;

    @Autowired
    CloudStorageService cloudStorageService;


    public void uploadFile(MultipartFile file, UUID ticketId) throws IOException {
        List<String> filenames=new ArrayList<>();
        cloudStorageService.uploadFile(file,filenames);
        TicketResult ticketResult=new TicketResult();
        ticketResult.setTicketId(ticketId);
        ticketResult.setFilenames(filenames.stream().collect(Collectors.joining(",")));
        ticketResultRepository.saveAndFlush(ticketResult);
    }

    public Resource loadFileAsResource(UUID resultId) {
        try {

            Optional<TicketResult> ticketResult = ticketResultRepository.findById(resultId);

            //  Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(ticketResult.get().getFilenames());
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