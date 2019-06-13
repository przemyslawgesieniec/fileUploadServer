package com.bp.fileUploadServer.service;

import com.bp.fileUploadServer.model.FileMetadata;
import com.bp.fileUploadServer.model.FileUploadQueueTask;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    private static final int TOTAL_UPLOAD_RESOURCES = 5;
    private BlockingQueue<FileUploadQueueTask> fileUploadQueue;
    private ExecutorService uploadPool;
    private ComputingService computingService;

    public FileUploadService(ComputingService computingService) {

        this.computingService = computingService;

        uploadPool = Executors.newFixedThreadPool(TOTAL_UPLOAD_RESOURCES);
        final Comparator<FileUploadQueueTask> taskPriority = Comparator.comparing(FileUploadQueueTask::getPriority);
        fileUploadQueue = new PriorityBlockingQueue<>(100, taskPriority);
    }

    public List<String> uploadFiles(List<MultipartFile> files, String user) throws InterruptedException {

        final Map<String, String> filesNames = files.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, FileUploadService::getFileContent));

        final List<FileUploadQueueTask> userTasksToExecute = fileUploadQueue
                .stream()
                .limit(TOTAL_UPLOAD_RESOURCES)
                .filter(task -> isTasksMatchingFilesNamesAndUserName(filesNames.keySet(), user, task))
                .collect(Collectors.toList());

        final List<Future<String>> futureFilesServerNames = uploadPool.invokeAll(userTasksToExecute);

        final List<String> filesServerNames = futureFilesServerNames
                .stream()
                .map(FileUploadService::getFuture)
                .collect(Collectors.toList());

        fileUploadQueue.removeAll(userTasksToExecute);

        return filesServerNames;
    }


    public List<String> submitForUploadPermission(List<FileMetadata> fileMetadataList) {

        computingService.countUserRequest(fileMetadataList.get(0).getUserName(),fileMetadataList.size());

        final List<FileUploadQueueTask> notQueuedTasks = fileUploadQueue
                .stream()
                .filter(e -> !fileMetadataList.contains(e.getFileMetadata()))
                .collect(Collectors.toList());

        notQueuedTasks.forEach(e -> {
            e.setPriority(computingService.computePriority(e));
            uploadPool.submit(e);
        });

        return fileUploadQueue
                .stream()
                .limit(TOTAL_UPLOAD_RESOURCES)
                .filter(task -> fileMetadataList.contains(task.getFileMetadata()))
                .map(task -> task.getFileMetadata().getFileName())
                .collect(Collectors.toList());
    }

    private boolean isTasksMatchingFilesNamesAndUserName(Set<String> filesNames, String userName, FileUploadQueueTask element) {
        return filesNames.contains(element.getFileMetadata().getFileName()) && userName.equals(element.getFileMetadata().getUserName());
    }

    private static String getFuture(Future<String> stringFuture) {
        try {
            return stringFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getFileContent(MultipartFile file) {

        try {
            return new String(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }




}
