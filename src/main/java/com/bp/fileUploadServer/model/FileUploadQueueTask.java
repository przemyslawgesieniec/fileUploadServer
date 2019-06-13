package com.bp.fileUploadServer.model;

import lombok.Getter;
import java.util.concurrent.Callable;

@Getter
public class FileUploadQueueTask implements Callable<String> {

    private Integer priority;
    private FileMetadata fileMetadata;

    @Override
    public String call() throws Exception {
        return null;
    }

}
