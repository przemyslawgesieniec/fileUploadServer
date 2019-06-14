package com.bp.fileUploadServer.model;

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
