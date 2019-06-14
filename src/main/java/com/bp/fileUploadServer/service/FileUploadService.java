package com.bp.fileUploadServer.service;

import com.bp.fileUploadServer.model.FileMetadata;
import com.bp.fileUploadServer.model.SnapshotContent;
import com.bp.fileUploadServer.model.Task.FileUploadQueueTask;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final long UPLOAD_QUEUE_TIMEOUT_SECONDS = 8000; //todo change
    private BlockingQueue<FileUploadQueueTask> fileUploadQueue;
    private Map<String, SnapshotContent> livingQueueSnapshot;
    private ExecutorService uploadPool;
    private ComputingService computingService;
    private DiscService discService;

    public FileUploadService(ComputingService computingService, DiscService discService) {

        this.computingService = computingService;
        this.discService = discService;

        uploadPool = Executors.newFixedThreadPool(TOTAL_UPLOAD_RESOURCES);
        livingQueueSnapshot = new ConcurrentHashMap<>();
        final Comparator<FileUploadQueueTask> taskPriority = Comparator.comparing(FileUploadQueueTask::getPriority);
        fileUploadQueue = new PriorityBlockingQueue<>(100, taskPriority);
    }


    public List<String> submitForUploadPermission(List<FileMetadata> fileMetadataList) {

        final List<FileUploadQueueTask> notQueuedTasks = new ArrayList<>();
        fileMetadataList.forEach(e -> notQueuedTasks.add(new FileUploadQueueTask(e, discService)));

        final List<FileUploadQueueTask> taskToBeQueued = notQueuedTasks
                .stream()
                .filter(e -> !fileUploadQueue.contains(e))
                .collect(Collectors.toList());

        taskToBeQueued.forEach(e -> {
            computingService.countUserRequest(e.getFileMetadata().getUserName());
            e.setPriority(computingService.computePriority(e));
            fileUploadQueue.add(e);
            System.out.println("UPLOAD - user" + e.getFileMetadata().getUserName() + " submitted to upload file " + e.getFileMetadata().getFileName() + " with priority: " + e.getPriority());
        });

        updateLivingSnapshot();

        final List<String> fileIdsReadyForUpload = livingQueueSnapshot.entrySet()
                .stream()
                .filter(snapshot -> fileMetadataList.contains(snapshot.getValue().getFileUploadQueueTask().getFileMetadata()))
                .peek(FileUploadService::logReadyForUploadForRequester)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return fileIdsReadyForUpload;
    }

    public List<String> uploadFiles(Map<String, MultipartFile> fileNameWithContent, String user) throws InterruptedException {

        final Map<String, SnapshotContent> filteredLivingQueueSnapshot = livingQueueSnapshot.entrySet().stream()
                .filter(task -> fileNameWithContent.keySet().contains(task.getKey()))
                .filter(task -> task.getValue().getFileUploadQueueTask().getFileMetadata().getUserName().equals(user))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        filteredLivingQueueSnapshot.forEach((k, v) -> {
            try {
                final MultipartFile multipartFile = fileNameWithContent.get(k);
                v.getFileUploadQueueTask().getFileMetadata().setFileContent(new String(multipartFile.getBytes()));
            } catch (IOException e) {
                System.out.println("empty file stored");
            }
        });

        final List<FileUploadQueueTask> userTasksToExecute = filteredLivingQueueSnapshot
                .entrySet()
                .stream()
                .map(e -> e.getValue().getFileUploadQueueTask())
                .collect(Collectors.toList());
        //todo verify, same files send again are not filtered out

        uploadPool.invokeAll(userTasksToExecute);

        filteredLivingQueueSnapshot.forEach((key, value) -> {
            livingQueueSnapshot.remove(key);
            System.out.println("UPLOAD - file " + value.getFileUploadQueueTask().getFileMetadata().getFileName()
                    + " of user " + value.getFileUploadQueueTask().getFileMetadata().getUserName() + " is uploaded");
        });

        return userTasksToExecute.stream().map(e->e.getFileMetadata().getServerFileName()).collect(Collectors.toList());
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

    private synchronized void updateLivingSnapshot() {

        cleanupLivingSnapshot();

        while (livingQueueSnapshot.size() < TOTAL_UPLOAD_RESOURCES) {
            if (fileUploadQueue.size() == 0) break;
            final FileUploadQueueTask uploadTask = fileUploadQueue.poll();
            final String uploadKey = uploadTask.getTaskId() + uploadTask.getFileMetadata().getFileName();
            livingQueueSnapshot.put(uploadKey, new SnapshotContent(uploadTask, Instant.now().getEpochSecond()));
        }

    }

    private synchronized void cleanupLivingSnapshot() {
        List<String> outdatedTasks = new ArrayList<>();
        livingQueueSnapshot.forEach((k, v) -> {
            final long epochSecond = Instant.now().getEpochSecond();
            if (epochSecond - v.getTimestamp() > UPLOAD_QUEUE_TIMEOUT_SECONDS) {
                outdatedTasks.add(k);
            }
        });
        outdatedTasks.forEach(e -> {
            livingQueueSnapshot.remove(e);
            System.out.println("UPLOAD - task " + e + " has been removed from queue due to its timeout");
        });
    }

    private static void logReadyForUploadForRequester(Map.Entry<String, SnapshotContent> e) {
        System.out.println("UPLOAD - server is ready for upload file " +
                e.getValue().getFileUploadQueueTask().getFileMetadata().getFileName() +
                " of user " + e.getValue().getFileUploadQueueTask().getFileMetadata().getUserName());
    }
}
