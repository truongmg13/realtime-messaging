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
            sendError("");
            return;
        }

        if (envelope.type() == null) {
            sendError("");
            return;
        }

        switch (envelope.type()) {
            case SEND -> handleSend(connection, envelope);
            default -> sendError("");
        }

    }

    private void handleSend(WebSocketConnection connection, Envelope envelope) {
        // check if user is authenticated and connection is open

        // validate recipientId content

        // try to send to client first
        Map<String, String> payload = Map.of("type", "MESSAGE", "content", "haha");
        try {
            connection.send(objectMapper.writeValueAsString(payload));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendError(String msg) {
        // TODO: implement sending error msg back to client
    }

}
