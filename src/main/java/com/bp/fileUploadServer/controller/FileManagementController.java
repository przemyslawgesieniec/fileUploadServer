package com.bp.fileUploadServer.controller;

import com.bp.fileUploadServer.download.FileDownloadService;
import com.bp.fileUploadServer.model.FileMetadata;
import com.bp.fileUploadServer.service.DiscService;
import com.bp.fileUploadServer.upload.FileUploadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileManagementController {

    private FileUploadService fileManageService;
    private FileDownloadService fileDownloadService;
    private DiscService discService;

    public FileManagementController(FileUploadService fileManageService,
                                    FileDownloadService fileDownloadService,
                                    DiscService discService) {
        this.fileManageService = fileManageService;
        this.fileDownloadService = fileDownloadService;
        this.discService = discService;
    }


    @PostMapping("/upload")
    public ResponseEntity uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("user") String user, @RequestParam("serverFilesId") List<String> serverFilesId) throws InterruptedException {

        Map<String, MultipartFile> fileMetadataMap = new HashMap<>();

        for (int i = 0; i < serverFilesId.size(); i++) {
            fileMetadataMap.put(serverFilesId.get(i), files.get(i));
        }

        final List<String> uploadedFilesServerNames = fileManageService.uploadFiles(fileMetadataMap, user);
        return ResponseEntity.ok(uploadedFilesServerNames);
    }

    @GetMapping("request/upload")
    public ResponseEntity requestUploadingFile(@RequestParam("filesNames") List<String> fileNames, @RequestParam("filesSizes") List<Integer> sizes, @RequestParam("user") String user) {

        List<FileMetadata> fileMetadataList = new ArrayList<>();
        for (int i = 0; i < fileNames.size(); i++) {
            fileMetadataList.add(new FileMetadata(fileNames.get(i), user, sizes.get(i)));
        }

        final List<String> filesKeysReadyForUpload = fileManageService.submitForUploadPermission(fileMetadataList);

        return ResponseEntity.ok(filesKeysReadyForUpload);
    }

    @GetMapping("/download")
    public ResponseEntity requestUploadingFile(@RequestParam("filesNames") List<String> serverFileNames,
                                               @RequestParam("user") String user,
                                               @RequestParam("filesSizes") List<Integer> sizes) throws InterruptedException {

        List<FileMetadata> fileMetadataList = new ArrayList<>();
        for (int i = 0; i < serverFileNames.size(); i++) {
            fileMetadataList.add(new FileMetadata(user, sizes.get(i), serverFileNames.get(i)));
        }

        final Map<String, String> fileNameFileContentMap = fileDownloadService.downloadFiles(fileMetadataList);

        return ResponseEntity.ok(fileNameFileContentMap);

    }

    @GetMapping("/get/filenames")
    public ResponseEntity requestUploadingFile(@RequestParam("user") String user) {
        return ResponseEntity.ok(discService.getAllUserFiles(user));
    }
}
