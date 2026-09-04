package com.truongmg.messaging.dto;

import com.truongmg.messaging.model.Message;
import com.truongmg.messaging.model.MessageStatus;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID senderId,
        UUID recipientId,
        String content,
        MessageStatus status,
        Instant sentAt,
        Instant deliveredAt) {

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getRecipient().getId(),
                message.getContent(),
                message.getStatus(),
                message.getSentAt(),
                message.getDeliveredAt());
    }
}
