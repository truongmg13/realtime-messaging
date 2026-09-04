package com.truongmg.messaging.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AttachmentServiceIntegrationTest {

    @TempDir
    static Path uploadDir;

    @DynamicPropertySource
    static void overrideUploadDir(DynamicPropertyRegistry registry) {
        // Point real LocalAttachmentStorage at a throwaway dir instead of the project's ./uploads,
        // so the test doesn't write into the working tree or depend on its state across runs.
        registry.add("app.attachment.upload-dir", uploadDir::toString);
    }

    @Autowired
    private AttachmentService attachmentService;

    @Test
    void upload_validFile_writesFileToDiskViaRealSpringContext() throws IOException {
        UUID uploaderId = UUID.randomUUID();
        byte[] content = "hello world".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "greeting.png", "image/png", content);

        attachmentService.upload(file, uploaderId);

        try (Stream<Path> files = Files.list(uploadDir)) {
            List<Path> stored = files.toList();
            assertThat(stored).hasSize(1);
            assertThat(stored.getFirst().toString()).endsWith(".png");
            assertThat(Files.readAllBytes(stored.getFirst())).isEqualTo(content);
        }
    }
}
