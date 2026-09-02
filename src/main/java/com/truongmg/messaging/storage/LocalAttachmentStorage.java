package com.truongmg.messaging.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
public class LocalAttachmentStorage implements AttachmentStorage {

    private final Path rootLocation;

    public LocalAttachmentStorage(@Value("{app.attachment.upload-dir:./uploads}") String uploadDir) throws IOException {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectory(this.rootLocation);
        log.info("Attachment storage initialized at {}", this.rootLocation);
    }

    @Override
    public String store(InputStream in, UUID attachmentId, String filename) throws IOException {
        String ext = extractExtension(filename);
        String storedFilename = attachmentId.toString() + ext;
        Path target = rootLocation.resolve(storedFilename);
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Stored attachment {} at {}", storedFilename, target);
        return storedFilename;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf("."));
    }

}
