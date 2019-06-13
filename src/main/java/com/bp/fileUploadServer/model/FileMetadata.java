package com.bp.fileUploadServer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class FileMetadata {

    String fileName;
    String userName;
    Integer fileSize;

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
