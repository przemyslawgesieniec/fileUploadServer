package com.bp.fileUploadServer.model;

import lombok.Getter;
import lombok.Setter;
import java.util.concurrent.Callable;

@Getter
public class FileUploadQueueTask implements Callable<String> {

    @Setter
    private long priority;

    private FileMetadata fileMetadata;

    @Override
    public String call() throws Exception {
        return null;
    }

}
