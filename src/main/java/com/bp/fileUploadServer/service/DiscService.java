package com.bp.fileUploadServer.service;

import com.bp.fileUploadServer.model.Disc;
import com.bp.fileUploadServer.model.FileMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class DiscService {

    private BlockingQueue<Disc> discs = new LinkedBlockingQueue<>(5);
    private List<String> csvMapperPaths;

    public DiscService() {
        for (int i = 0; i < 5; i++) {
            discs.add(new Disc(i + 1));
        }

        csvMapperPaths = new ArrayList<>();
        discs.forEach(d -> csvMapperPaths.add(d.getCsvMapperPath()));
    }

    public void save(FileMetadata fileMetadata) throws InterruptedException {

        System.out.println("Disc - saving...");
        final Disc polledDisc = discs.poll();
        polledDisc.save(fileMetadata);
        discs.add(polledDisc);
        System.out.println("Disc - saved");

    }

    public FileMetadata download(FileMetadata fileMetadata) {

        return discs.stream()
                .filter(disc -> disc.hasFile(fileMetadata.getServerFileName()))
                .findFirst()
                .map(disc -> disc.download(fileMetadata))
                .get();

    }

    public synchronized List<String> getAllUserFiles(final String userName) {

        final List<String> fileNamesForUser = new ArrayList<>();

        csvMapperPaths.forEach(path -> {

            try (Stream<String> stream = Files.lines(Paths.get(path))) {

                final List<String> collect = stream
                        .filter(e -> userName.equals(Arrays.asList(e.split(",")).get(1)))
                        .map(DiscService::getServerFileName)
                        .collect(Collectors.toList());

                fileNamesForUser.addAll(collect);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return fileNamesForUser;
    }

    private static String getServerFileName(String e) {
        return Arrays.asList(e.split(",")).get(0);
    }

}
