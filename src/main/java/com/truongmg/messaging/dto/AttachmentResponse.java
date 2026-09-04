package com.truongmg.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String url,
        Instant uploadedAt) {
}
