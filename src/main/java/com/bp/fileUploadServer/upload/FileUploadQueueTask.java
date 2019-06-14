package com.bp.fileUploadServer.upload;

import com.bp.fileUploadServer.model.FileMetadata;
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
        fileMetadata.setServerFileName(taskId);
        discService.save(fileMetadata);
        return fileMetadata.getServerFileName();
    }

    @Override
    public boolean equals(Object o) {
        final boolean equalsNames = ((FileUploadQueueTask) o).getFileMetadata().getUserName().equals(fileMetadata.getUserName());
        final boolean equalsFileNames = ((FileUploadQueueTask) o).getFileMetadata().getFileName().equals(fileMetadata.getFileName());
        return equalsNames && equalsFileNames;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileMetadata);
    }
}
