package com.truongmg.messaging.websocket.session;

import com.truongmg.messaging.model.Message;
import lombok.Getter;

import java.io.IOException;
import java.util.UUID;

/**
 * An authenticated, live WebSocket session.
 * Wraps the userId and the raw connection used to send frames.
 */
@Getter
public class WebSocketSession {

    private final UUID userId;
    private final WebSocketSender sender;

    public WebSocketSession(UUID userId, WebSocketSender sender) {
        this.userId = userId;
        this.sender = sender;
    }

    /**
     * Sends a JSON text frame to this user's connection.
     */
    public void send(String json) throws IOException {
        sender.send(json);
    }

    /** Functional interface so WebSocketConnection can expose send() without a circular import */
    @FunctionalInterface
    public interface WebSocketSender {
        void send(String json) throws IOException;
    }
}
