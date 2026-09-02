package com.truongmg.messaging.controller;

import com.truongmg.messaging.dto.AttachmentResponse;
import com.truongmg.messaging.model.Attachment;
import com.truongmg.messaging.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentResponse uploadAttachment(
            @RequestParam("file") MultipartFile file) {
        Attachment attachment = attachmentService.upload(file);
        return new AttachmentResponse();
    }

}
