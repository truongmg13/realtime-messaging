package com.truongmg.messaging.service;

import com.truongmg.messaging.dto.AttachmentResponse;
import com.truongmg.messaging.exception.BadRequestException;
import com.truongmg.messaging.model.Attachment;
import com.truongmg.messaging.repository.AttachmentRepository;
import com.truongmg.messaging.storage.AttachmentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    @Value("${app.attachment.max-file-size-bytes}")
    private long maxFileSizeBytes;

    private final AttachmentRepository attachmentRepository;
    private final AttachmentStorage attachmentStorage;

    public AttachmentResponse upload(MultipartFile file, UUID uploaderId) {
        validateFile(file);

        UUID storageId = UUID.randomUUID();
        String originalFilename = sanitise(file.getOriginalFilename());
        String storagePath;
        try {
            storagePath = attachmentStorage.store(file.getInputStream(), storageId, originalFilename);
            log.info("Stored attachment {} at {}", originalFilename, storagePath);
        } catch (IOException e) {
            log.error("Failed to store attachment {}: {}", originalFilename, e.getMessage(), e);
            throw new RuntimeException("File storage failed - please try again", e);
        }

        Attachment attachment = new Attachment();
        attachment.setId(storageId);
        attachment.setUploadId(uploaderId);
        attachment.setOriginalFilename(originalFilename);
        attachment.setContentType(file.getContentType());
        attachment.setSizeBytes(file.getSize());
        attachment.setStoragePath(storagePath);


        try {
            attachmentRepository.save(attachment);
        } catch (Exception e) {
            // compensating action: remove the orphaned file
            try { attachmentStorage.delete(storagePath); } catch (IOException ex) {
                log.error("Failed to delete orphaned file {}: {}", storagePath, ex.getMessage(), ex);
            }
            throw e;
        }

        return buildResponse(attachment);
    }

    private AttachmentResponse buildResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getStoragePath(),
                attachment.getUploadedAt()
        );
    }

    private String sanitise(@Nullable String fileName) {
        // TODO: cleanse the filename to prevent path traversal and other security issues
        return fileName;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or null");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BadRequestException(
                    "File size %d bytes exceeds the maximum allowed %d bytes"
                            .formatted(file.getSize(), maxFileSizeBytes)
            );
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "File type %s not allowed. Allowed types %s"
                            .formatted(contentType, String.join(", ", ALLOWED_TYPES))
            );
        }
    }
}
