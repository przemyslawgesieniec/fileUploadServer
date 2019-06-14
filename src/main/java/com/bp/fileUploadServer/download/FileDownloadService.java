package com.bp.fileUploadServer.download;

import com.bp.fileUploadServer.model.FileMetadata;
import com.bp.fileUploadServer.service.ComputingService;
import com.bp.fileUploadServer.service.DiscService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

@Service
public class FileDownloadService {

    private static final int TOTAL_DOWNLOAD_RESOURCES = 5;
    private static final long DOWNLOAD_QUEUE_TIMEOUT_SECONDS = 8000; //todo change
    private BlockingQueue<FileDownloadQueueTask> fileDownloadQueue;
    private Map<String, DownloadSnapshotContent> livingQueueSnapshot;
    private ExecutorService downloadPool;
    private ComputingService computingService;
    private DiscService discService;

    public FileDownloadService(ComputingService computingService, DiscService discService) {

        this.computingService = computingService;
        this.discService = discService;
        downloadPool = Executors.newFixedThreadPool(TOTAL_DOWNLOAD_RESOURCES);
        livingQueueSnapshot = new ConcurrentHashMap<>();
        final Comparator<FileDownloadQueueTask> taskPriority = Comparator.comparing(FileDownloadQueueTask::getPriority);
        fileDownloadQueue = new PriorityBlockingQueue<>(100, taskPriority);
    }


    public Map<String,String> downloadFiles(List<FileMetadata> fileMetadataList) throws InterruptedException {

        final List<FileDownloadQueueTask> notQueuedTasks = new ArrayList<>();
        fileMetadataList.forEach(e -> notQueuedTasks.add(new FileDownloadQueueTask(e, discService)));

        final List<FileDownloadQueueTask> taskToBeQueued = notQueuedTasks
                .stream()
                .filter(e -> !fileDownloadQueue.contains(e))
                .collect(Collectors.toList());

        taskToBeQueued.forEach(e -> {
            computingService.countUserRequest(e.getFileMetadata().getUserName());
            e.setPriority(computingService.computePriority(e));
            fileDownloadQueue.add(e);
            System.out.println("DOWNLOAD - user" + e.getFileMetadata().getUserName() + " submitted to download file " + e.getFileMetadata().getFileName() + " with priority: " + e.getPriority());
        });

        updateLivingSnapshot();

        final List<String> fileIdsReadyForDownload = livingQueueSnapshot.entrySet()
                .stream()
                .filter(snapshot -> fileMetadataList.contains(snapshot.getValue().getFileDownloadQueueTask().getFileMetadata()))
                .peek(FileDownloadService::logReadyForForDownloadForRequester)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());


        if(fileIdsReadyForDownload.isEmpty()){
            return new HashMap<>();
        }

        final List<FileDownloadQueueTask> readyToDownloadQueueTask = livingQueueSnapshot
                .entrySet()
                .stream()
                .filter(e -> fileIdsReadyForDownload.contains(e.getKey()))
                .map(e -> e.getValue().getFileDownloadQueueTask())
                .collect(Collectors.toList());


        Map<String,String> fileNameFileContentMap = new HashMap<>();

        if(!readyToDownloadQueueTask.isEmpty()){
            final List<Future<FileMetadata>> futures = downloadPool.invokeAll(readyToDownloadQueueTask);
            futures.forEach(f->{
                try {
                    fileNameFileContentMap.put(f.get().getServerFileName(),f.get().getFileContent());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }

        return fileNameFileContentMap;
    }

    private synchronized void updateLivingSnapshot() {

        cleanupLivingSnapshot();

        while (livingQueueSnapshot.size() < TOTAL_DOWNLOAD_RESOURCES) {
            if (fileDownloadQueue.size() == 0) break;
            final FileDownloadQueueTask downloadQueueTask = fileDownloadQueue.poll();
            final String downloadKey = downloadQueueTask.getFileMetadata().getServerFileName();
            livingQueueSnapshot.put(downloadKey, new DownloadSnapshotContent(downloadQueueTask, Instant.now().getEpochSecond()));
        }

    }

    private synchronized void cleanupLivingSnapshot() {
        List<String> outdatedTasks = new ArrayList<>();
        livingQueueSnapshot.forEach((k, v) -> {
            final long epochSecond = Instant.now().getEpochSecond();
            if (epochSecond - v.getTimestamp() > DOWNLOAD_QUEUE_TIMEOUT_SECONDS) {
                outdatedTasks.add(k);
            }
        });
        outdatedTasks.forEach(e -> {
            livingQueueSnapshot.remove(e);
            System.out.println("DOWNLOAD - task " + e + " has been removed from queue due to its timeout");
        });
    }

    private static void logReadyForForDownloadForRequester(Map.Entry<String, DownloadSnapshotContent> e) {
        System.out.println("DOWNLOAD - file is ready to be downloaded" +
                e.getValue().getFileDownloadQueueTask().getFileMetadata().getServerFileName() +
                " of user " + e.getValue().getFileDownloadQueueTask().getFileMetadata().getUserName());
    }


}
