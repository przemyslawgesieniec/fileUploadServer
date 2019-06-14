package com.bp.fileUploadServer.service;

import com.bp.fileUploadServer.model.Disc;
import com.bp.fileUploadServer.model.FileMetadata;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;

@Service
public class DiscService {

    private BlockingQueue<Disc> discs = new LinkedBlockingQueue<>(5);

    public DiscService() {
        for (int i = 0; i < 5; i++) {
            discs.add(new Disc(i + 1));
        }
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
}
