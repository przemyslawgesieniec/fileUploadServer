package com.bp.fileUploadServer.service;

import com.bp.fileUploadServer.download.FileDownloadQueueTask;
import com.bp.fileUploadServer.upload.FileUploadQueueTask;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class ComputingService {

    private Map<String, Integer> requestCounter;

    public ComputingService() {
        requestCounter = new ConcurrentHashMap<>();
    }

    public long computePriority(FileUploadQueueTask fileUploadQueueTask) {
        return (fileUploadQueueTask.getFileMetadata().getFileSize() + Instant.now().getEpochSecond()) * requestCounter.get(fileUploadQueueTask.getFileMetadata().getUserName());
    }

    public long computePriority(FileDownloadQueueTask fileDownloadQueueTask) {
        return (fileDownloadQueueTask.getFileMetadata().getFileSize() + Instant.now().getEpochSecond()) * requestCounter.get(fileDownloadQueueTask.getFileMetadata().getUserName());
    }

    public void countUserRequest(String userName) {
        countUserRequest(userName,1);
    }
    public void countUserRequest(String userName, int numberOfRequestedFiles) {
        requestCounter.merge(userName, numberOfRequestedFiles, (currentValue, increment) -> currentValue + increment);
    }
}
