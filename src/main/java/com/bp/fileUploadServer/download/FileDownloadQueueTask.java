package com.bp.fileUploadServer.download;

import com.bp.fileUploadServer.model.FileMetadata;
import com.bp.fileUploadServer.service.DiscService;
import lombok.Getter;
import lombok.Setter;
import java.util.concurrent.Callable;

@Getter
public class FileDownloadQueueTask implements Callable<FileMetadata> {

    @Setter
    private long priority;
    private FileMetadata fileMetadata;
    private DiscService discService;

    public FileDownloadQueueTask(FileMetadata fileMetadata, DiscService discService) {
        this.fileMetadata = fileMetadata;
        this.discService = discService;
    }

    @Override
    public FileMetadata call() throws Exception {
        return discService.download(fileMetadata);
    }
}
