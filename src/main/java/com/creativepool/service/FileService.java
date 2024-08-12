package com.creativepool.service;


import com.creativepool.constants.Errors;
import com.creativepool.entity.TicketResult;
import com.creativepool.exception.ResourceNotFoundException;
import com.creativepool.repository.TicketResultRepository;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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


    public byte[] downloadAndZipFiles(UUID resultId) throws IOException {
        Optional<TicketResult> ticketResult = ticketResultRepository.findById(resultId);
        if (ticketResult.isEmpty())
            throw new ResourceNotFoundException(Errors.E00010.getMessage());

        String fileNames = ticketResult.get().getFilenames();
        String[] filenameArray = fileNames.split(",");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);

        for (String fileName : filenameArray) {
            Blob blob = cloudStorageService.getBlob(fileName);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            zipOut.write(blob.getContent());
            zipOut.closeEntry();
        }

        zipOut.close();
        return byteArrayOutputStream.toByteArray();
    }
}