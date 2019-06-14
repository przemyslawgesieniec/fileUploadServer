package com.bp.fileUploadServer.model;

import lombok.Getter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

@Getter
public class Disc {

    private String path;
    private String directoryPath;
    private String csvFilePath;
    private int discNumber;

    public Disc(int discNumber) {
        this.discNumber = discNumber;
        directoryPath ="src/main/resources/cluster/disc" + discNumber;
        path = directoryPath + "/";
        csvFilePath = path + "mapper.csv";
    }

    public void save(FileMetadata fileMetadata) throws InterruptedException {

        Thread.sleep(fileMetadata.getFileSize());
        saveFile(fileMetadata);
        System.out.println("DISC: file of user: " + fileMetadata.getUserName() + " saved properly");
        updateCsvFile(fileMetadata.getFileName(), fileMetadata.getServerFileName(), fileMetadata.getUserName());

    }

//    public UserFileData read(final QueuedUserRequest userRequest) {
//
//        stubProcessingTime(userRequest.getFileProcessingTime());
//        String fileContent = getFileContent(userRequest.getFileName());
//        System.out.println("DISC: file "+userRequest.getFileName() +" of user: " + userRequest.getUser() + " downloaded properly");
//        return new UserFileData(userRequest.getFileProcessingTime(), fileContent, userRequest.getFileName());
//    }

    private String getFileContent(String fileServerName) {

        File directory = new File(directoryPath);

        final String fileContent = String.join("\n", Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(file -> file.getName().equals(fileServerName))
                .findFirst().map(file -> {
                    try {
                        return Files.readAllLines(Paths.get(file.toURI()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .get());


        return fileContent;
    }

    private synchronized void updateCsvFile(final String originalFileName, final String serverFileName, final String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, true))) {
            writer.append(serverFileName).append(",").append(username).append(",").append(originalFileName);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(FileMetadata fileMetadata) {

        String filename = StringUtils.cleanPath(fileMetadata.getServerFileName());
        Path filepath = Paths.get(path + filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filepath)) {
            writer.write(fileMetadata.getFileContent());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean hasFile(String fileName) {

        try (Stream<String> lines = Files.lines(Paths.get(csvFilePath))) {
            return lines.anyMatch(line -> line.contains(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public FileMetadata download(FileMetadata fileMetadata) {

        try {
            Thread.sleep(fileMetadata.getFileSize());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String fileContent = getFileContent(fileMetadata.getServerFileName());
        fileMetadata.setFileContent(fileContent);
        return fileMetadata;
    }
}
