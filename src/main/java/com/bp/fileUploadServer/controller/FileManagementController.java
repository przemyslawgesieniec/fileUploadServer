package com.bp.fileUploadServer.controller;

import com.bp.fileUploadServer.model.FileMetadata;
import com.bp.fileUploadServer.service.FileUploadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileManagementController {

    FileUploadService fileManageService;

    public FileManagementController(FileUploadService fileManageService) {
        this.fileManageService = fileManageService;
    }

    @PostMapping("/upload")
    public ResponseEntity uploadFiles(@RequestParam("file") List<MultipartFile> files, @RequestParam("user") String user) throws InterruptedException {

        final List<String> uploadedFilesServerNames = fileManageService.uploadFiles(files, user);
        return ResponseEntity.ok(uploadedFilesServerNames);
    }

    @PostMapping("request/upload")
    public ResponseEntity requestUploadingFile(@RequestParam("fileName") Map<String, Integer> filesNamesWithSizes, @RequestParam("user") String user) {

        List<FileMetadata> fileMetadataList = new ArrayList<>();
        filesNamesWithSizes.forEach((fileName, size) -> fileMetadataList.add(new FileMetadata(fileName, user, size)));

        final List<String> filesNamesReadyForUpload = fileManageService.submitForUploadPermission(fileMetadataList);

        return ResponseEntity.ok(filesNamesReadyForUpload);
    }


}
