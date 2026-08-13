package com.truongmg.messaging.protocol;

public record Envelope(
        MessageType type,
        String recipientId,
        String content) { }
