package com.bp.fileUploadServer.model;

import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

@Getter
public class FileMetadata {

    private String fileName;

    public void setServerFileName(String uuid) {
        this.serverFileName = uuid + fileName;
    }

    private String serverFileName;
    private String userName;
    private Integer fileSize;
    @Setter
    private String fileContent;

    public FileMetadata(String fileName, String userName, Integer fileSize) {
        this.fileName = fileName;
        this.userName = userName;
        this.fileSize = fileSize;
    }

    public FileMetadata(String userName, Integer fileSize, String serverFileName) {
        this.serverFileName = serverFileName;
        this.userName = userName;
        this.fileSize = fileSize;
        this.fileName = serverFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(fileSize, that.fileSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, userName, fileSize);
    }
}
