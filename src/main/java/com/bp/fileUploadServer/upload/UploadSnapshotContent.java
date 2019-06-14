package com.bp.fileUploadServer.upload;

import com.bp.fileUploadServer.upload.FileUploadQueueTask;
import lombok.Getter;

@Getter
public class UploadSnapshotContent {

    private FileUploadQueueTask fileUploadQueueTask;
    private long timestamp;

    public UploadSnapshotContent(FileUploadQueueTask fileUploadQueueTask, long timestamp) {
        this.fileUploadQueueTask = fileUploadQueueTask;
        this.timestamp = timestamp;
    }
}
