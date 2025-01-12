package com.creativepool.service;


import com.creativepool.constants.Errors;
import com.creativepool.constants.Status;
import com.creativepool.entity.TicketResult;
import com.creativepool.exception.CreativePoolException;
import com.creativepool.exception.ResourceNotFoundException;
import com.creativepool.models.PaginatedResponse;
import com.creativepool.repository.TicketResultRepository;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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

//    @Autowired
//    CloudStorageService cloudStorageService;

    Logger logger= LoggerFactory.getLogger(FileService.class);


//    public void uploadFile(List<MultipartFile> files, UUID
//                           ) throws IOException {
//        List<String> filenames=new ArrayList<>();
//
//        if (tick
//        etDTO.getDeleteUrls() != null && !ticketDTO.getDeleteUrls().isEmpty()) {
//            for (String url : ticketDTO.getDeleteUrls()) {
//                String filename=cloudStorageService.getFilenameFromSignedUrl(url);
//                cloudStorageService.deleteFileUsingSignedUrl(url);
//                currentImages.remove(filename);
//            }
//        }
//
//
//        if (files != null && !files.isEmpty()) {
//            List<String> newUploadedUrls = new ArrayList<>();
//            for (MultipartFile file : files) {
//                cloudStorageService.uploadFile(file, newUploadedUrls);
//            }
//            currentImages.addAll(newUploadedUrls);
//        }
//
//        cloudStorageService.uploadFile(file,filenames);
//        String fileExtension=getFileExtension(file);
//        System.out.println("Hello"+fileExtension);
//        String fileNm=filenames.get(0)+"."+fileExtension;
//
//
//        filenames.remove(0);
//        filenames.add(fileNm);
//
//        TicketResult ticketResult=new TicketResult();
//        ticketResult.setTicketId(ticketId);
//        ticketResult.setFilenames(filenames.stream().collect(Collectors.joining(",")));
//        ticketResultRepository.saveAndFlush(ticketResult);
//    }
//
//    public Resource loadFileAsResource(UUID resultId) {
//        try {
//
//            Optional<TicketResult> ticketResult = ticketResultRepository.findById(resultId);
//
//            //  Path filePath = fileStorageLocation.resolve(fileName).normalize();
//            Resource resource = new UrlResource(ticketResult.get().getFilenames());
//            if (resource.exists()) {
//                return resource;
//            } else {
//                throw new RuntimeException("File not found ");
//            }
//        } catch (MalformedURLException ex) {
//            throw new RuntimeException("File not found", ex);
//        }
//    }
//
//
//    public byte[] downloadAndZipFiles(UUID resultId) throws IOException {
//        Optional<TicketResult> ticketResult = ticketResultRepository.findById(resultId);
//        if (ticketResult.isEmpty())
//            throw new ResourceNotFoundException(Errors.E00010.getMessage());
//
//        String fileNames = ticketResult.get().getFilenames();
//        String[] filenameArray = fileNames.split(",");
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);
//
//        for (String fileName : filenameArray) {
//            int lastDotIndex = fileName.lastIndexOf('.');
//            String updatedFileName="";
//            if (lastDotIndex > 0) {
//                updatedFileName=fileName.substring(0, lastDotIndex);
//            }
//            Blob blob = cloudStorageService.getBlob(updatedFileName);
//
//            System.out.println("Hello"+blob.getContentType());
//
//            ZipEntry zipEntry = new ZipEntry(fileName);
//            zipOut.putNextEntry(zipEntry);
//            zipOut.write(blob.getContent());
//            zipOut.closeEntry();
//        }
//
//        zipOut.close();
//        return byteArrayOutputStream.toByteArray();
//    }
//
//    public String getFileExtension(MultipartFile file) {
//        String originalFilename = file.getOriginalFilename();
//        if (originalFilename != null && originalFilename.contains(".")) {
//            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
//        }
//        return null;
//    }


    public TicketResult updateTicketResult(String filename, UUID ticketId, Status status) throws IOException {
        TicketResult ticketResult = null;
        try {
            if (status.equals(Status.FAILURE)) {
                ticketResultRepository.deleteTicketResult(ticketId, status, filename);
            } else {
                ticketResult = ticketResultRepository.findByFileName(filename);

                if (!ObjectUtils.isEmpty(ticketResult)) {
                    ticketResult.setStatus(Status.SUCCESS);
                } else {
                    ticketResult = new TicketResult();
                    ticketResult.setTicketId(ticketId);
                    ticketResult.setFilename(filename);
                    ticketResult.setLastUpload(new Date());
                    ticketResult.setStatus(status);
                    ticketResultRepository.saveAndFlush(ticketResult);
                }
            }
            return ticketResult;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new CreativePoolException(Errors.E00013.getMessage());
        }
    }

    public PaginatedResponse<TicketResult> getTicketResult(UUID ticketId,Integer page,Integer size) throws IOException {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "last_Upload"));
            Page<TicketResult> ticketResult= ticketResultRepository.getTicketResult(ticketId,pageable);
            List<TicketResult> ticketResultList=ticketResult.stream().collect(Collectors.toList());

            return new PaginatedResponse<>(ticketResult.getTotalElements(), ticketResultList, ((page + 1) * size >= ticketResult.getTotalElements()), page + 1, ticketResult.getTotalPages());

        }catch (Exception ex){
            logger.error(ex.getMessage());
            throw new CreativePoolException(Errors.E00013.getMessage());
        }
    }
}