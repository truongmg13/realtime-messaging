package com.truongmg.messaging.dto;

import java.util.List;

public record ConversationPageResponse(
        List<MessageResponse> messages,
        int page,
        int size,
        long totalElements,
        boolean hasMore) {
}
