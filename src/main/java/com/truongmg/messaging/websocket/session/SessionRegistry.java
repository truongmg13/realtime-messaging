package com.truongmg.messaging.websocket.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single source of truth for which users currently have an open WebSocket session.
 *
 * Thread-safe: backed by ConcurrentHashMap
 */
@Slf4j
@Component
public class SessionRegistry {

    private final ConcurrentHashMap<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(UUID userId, WebSocketSession session) {
        sessions.put(userId, session);
        log.info("Session registered for user {}, total online: {}", userId, sessions.size());
    }

    public Optional<WebSocketSession> find(UUID userId) {
        return Optional.ofNullable(sessions.get(userId));
    }
}
