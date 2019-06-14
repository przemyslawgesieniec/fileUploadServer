package com.bp.fileUploadServer.model;

import com.bp.fileUploadServer.model.Task.FileUploadQueueTask;
import lombok.Getter;

@Getter
public class SnapshotContent {

    private FileUploadQueueTask fileUploadQueueTask;
    private long timestamp;

    public SnapshotContent(FileUploadQueueTask fileUploadQueueTask, long timestamp) {
        this.fileUploadQueueTask = fileUploadQueueTask;
        this.timestamp = timestamp;
    }
}
