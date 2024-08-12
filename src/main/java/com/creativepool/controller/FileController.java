package com.creativepool.controller;


import com.creativepool.constants.Errors;
import com.creativepool.exception.CreativePoolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import com.creativepool.service.FileService;


import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creative-pool/files")
public class FileController{

    @Autowired
    private FileService fileService;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Value("${credential.file}")
    private String credentials;

    @Value("${project.id}")
    private String projectId;


    @PostMapping("/result-upload")
    public void uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("ticketId") UUID ticketId) {
        try {
            fileService.uploadFile(file,ticketId);

        } catch (IOException e) {
            throw new CreativePoolException(Errors.E00009.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile( @RequestParam("resultId") UUID resultId) {
        Resource resource = fileService.loadFileAsResource(resultId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    @GetMapping("/download-zip")
    public ResponseEntity<byte[]> downloadFilesAsZip(
            @RequestParam("resultId") UUID resultId) {

        try {
            byte[] zipFile = fileService.downloadAndZipFiles(resultId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"files.zip\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            return new ResponseEntity<>(zipFile, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
