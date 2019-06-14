package com.bp.fileUploadServer.download;

import lombok.Getter;

@Getter
public class DownloadSnapshotContent {

    private FileDownloadQueueTask fileDownloadQueueTask;
    private long timestamp;

    public DownloadSnapshotContent(FileDownloadQueueTask fileDownloadQueueTask, long timestamp) {
        this.fileDownloadQueueTask = fileDownloadQueueTask;
        this.timestamp = timestamp;
    }
}
