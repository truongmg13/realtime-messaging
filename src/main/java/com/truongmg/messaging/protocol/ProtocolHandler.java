package com.truongmg.messaging.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.truongmg.messaging.websocket.WebSocketConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProtocolHandler {

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

    }

    private void handleSend(WebSocketConnection connection, Envelope envelope) {
        // check if user is authenticated and connection is open

        // validate recipientId content

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
