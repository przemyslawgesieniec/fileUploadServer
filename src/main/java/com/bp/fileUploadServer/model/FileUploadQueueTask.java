package com.bp.fileUploadServer.model;

import com.bp.fileUploadServer.service.DiscService;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;

@Getter
public class FileUploadQueueTask implements Callable<String> {

    @Setter
    private long priority;
    private FileMetadata fileMetadata;
    private DiscService discService;
    private String taskId;

    public FileUploadQueueTask(FileMetadata fileMetadata, DiscService discService) {
        this.fileMetadata = fileMetadata;
        this.discService = discService;
        this.taskId = UUID.randomUUID().toString();
    }

    @Override
    public String call() throws Exception {

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileUploadQueueTask that = (FileUploadQueueTask) o;
        return Objects.equals(fileMetadata, that.fileMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileMetadata);
    }
}
