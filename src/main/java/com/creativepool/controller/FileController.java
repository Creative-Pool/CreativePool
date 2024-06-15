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
import java.util.UUID;

@RestController
@RequestMapping("/creative-pool/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Value("${credential.file}")
    private String credentials;

    @Value("${project.id}")
    private String projectId;


    @PostMapping("/upload")
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


}
