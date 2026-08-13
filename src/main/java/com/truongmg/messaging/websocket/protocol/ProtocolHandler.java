package com.truongmg.messaging.websocket.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.truongmg.messaging.model.Message;
import com.truongmg.messaging.security.JwtUtil;
import com.truongmg.messaging.service.MessageService;
import com.truongmg.messaging.websocket.session.SessionRegistry;
import com.truongmg.messaging.websocket.session.WebSocketSession;
import com.truongmg.messaging.websocket.server.WebSocketConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProtocolHandler {

    private final JwtUtil jwtUtil;
    private final SessionRegistry sessionRegistry;
    private final MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handleMessage(WebSocketConnection connection, String json) {
        Envelope envelope;
        try {
            envelope = objectMapper.readValue(json, Envelope.class);
            log.info("Envelop: {}", envelope);
        } catch (JsonProcessingException e) {
            sendError(connection, "INVALID_JSON", "Could not parse message envelope");
            return;
        }

        MessageType type = envelope.type();
        if (type == null) {
            sendError(connection, "MISSING_TYPE", "Message type is required");
            return;
        }

        switch (type) {
            case AUTH -> handleAuth(connection, envelope);
            case SEND -> handleSend(connection, envelope);
            default -> sendError(connection, "UNKNOWN_TYPE", "Unsupported type: " + type);
        }

    }

    private void handleAuth(WebSocketConnection connection, Envelope envelope) {
        if (connection.isAuthenticated()) {
            sendError(connection, "ALREADY_AUTH", "Already authenticated");
            return;
        }

        String token = envelope.token();
        if (token == null || token.isBlank()) {
            sendError(connection, "AUTH_FAILED", "Token is required");
            return;
        }

        UUID userId;
        try {
            userId = jwtUtil.extractUserId(token);
        } catch (Exception e) {
            log.debug("WebSocket auth rejected - invalid jwt: {}", e.getMessage());
            sendError(connection, "AUTH_FAILED", "Invalid or expired token");
            return;
        }

        // register session
        WebSocketSession session = new WebSocketSession(userId);
        sessionRegistry.register(userId, session);
        connection.updateAuthDetails(userId);

        sendJson(connection, Map.of("type", "AUTH_OK", "userId", userId.toString()));
        log.info("User {} authenticated via WebSocket", userId);
    }

    private void handleSend(WebSocketConnection connection, Envelope envelope) {
        // check if user is authenticated and connection is open
        if (!connection.isAuthenticated()) {
            sendError(connection, "NOT_AUTHENTICATED", "Send AUTH first");
            return;
        }

        // validate recipientId & content
        String content = envelope.content();
        if (envelope.recipientId() == null || content == null || content.isBlank()) {
            sendError(connection, "INVALID_PAYLOAD", "recipientId and content are required");
            return;
        }

        UUID senderId;
        UUID recipientId;
        try {
            senderId = connection.getAuthenticatedUserId();
            recipientId = UUID.fromString(envelope.recipientId());
        } catch (IllegalArgumentException e) {
            sendError(connection, "INVALID_PAYLOAD", "recipientId must be a valid UUID");
            return;
        }

        // Save message
        Message message;
        message = messageService.save(senderId, recipientId, content);

        // try to send to client first
        Map<String, String> payload = Map.of("type", "MESSAGE", "content", "haha");
        sendJson(connection, payload);

    }

    private void sendJson(WebSocketConnection connection, Map<String, String> payload) {
        try {
            connection.send(objectMapper.writeValueAsString(payload));
        } catch (IOException e) {
            log.debug("Could not write to connection: {}", e.getMessage());
        }
    }

    private void sendError(WebSocketConnection connection, String code, String reason) {
        sendJson(
            connection,
            Map.of("type", "ERROR", "code", code, "reason", reason)
        );
    }

}
