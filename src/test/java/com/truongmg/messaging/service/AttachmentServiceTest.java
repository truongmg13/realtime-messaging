package com.truongmg.messaging.service;

import com.truongmg.messaging.exception.BadRequestException;
import com.truongmg.messaging.repository.AttachmentRepository;
import com.truongmg.messaging.storage.AttachmentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    private static final long MAX_FILE_SIZE_BYTES = 1024;

    @Mock private AttachmentRepository attachmentRepository;
    @Mock private AttachmentStorage attachmentStorage;

    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentService(attachmentRepository, attachmentStorage);
        ReflectionTestUtils.setField(attachmentService, "maxFileSizeBytes", MAX_FILE_SIZE_BYTES);
    }

    @Test
    void upload_validFile_storesFileWithSanitisedNameAndRandomId() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "content".getBytes());
        when(attachmentStorage.store(any(), any(), eq("photo.png"))).thenReturn("/uploads/some-path");

        attachmentService.upload(file);

        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(attachmentStorage).store(any(), idCaptor.capture(), eq("photo.png"));
        assertThat(idCaptor.getValue()).isNotNull();
    }

    @Test
    void upload_nullFile_throwsBadRequestWithoutStoring() {
        assertThatThrownBy(() -> attachmentService.upload(null))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(attachmentStorage);
    }

    @Test
    void upload_emptyFile_throwsBadRequestWithoutStoring() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> attachmentService.upload(file))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(attachmentStorage);
    }

    @Test
    void upload_fileExceedsMaxSize_throwsBadRequestWithoutStoring() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.png", "image/png", new byte[(int) MAX_FILE_SIZE_BYTES + 1]);

        assertThatThrownBy(() -> attachmentService.upload(file))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(attachmentStorage);
    }

    @Test
    void upload_disallowedContentType_throwsBadRequestWithoutStoring() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.js", "application/javascript", "content".getBytes());

        assertThatThrownBy(() -> attachmentService.upload(file))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(attachmentStorage);
    }

    @Test
    void upload_storageThrowsIOException_wrapsInRuntimeException() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "content".getBytes());
        when(attachmentStorage.store(any(), any(), eq("photo.png")))
                .thenThrow(new IOException("disk full"));

        assertThatThrownBy(() -> attachmentService.upload(file))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }
}
