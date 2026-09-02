package com.truongmg.messaging.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
public class Attachment {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "upload_id", nullable = false, updatable = false)
    private UUID uploadId;

    @Column(name = "original_filename", nullable = false, updatable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, updatable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private long sizeBytes;

    @Column(name = "storage_path", nullable = false, updatable = false, length = 500)
    private String storagePath;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @PrePersist
    private void prePersist() {
        this.uploadedAt = Instant.now();
    }

}
