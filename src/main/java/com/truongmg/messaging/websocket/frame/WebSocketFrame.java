package com.truongmg.messaging.websocket.frame;

import java.nio.charset.StandardCharsets;

/**
 * Represents a single decoded WebSocket frame per RFC 6455 Section 5.
 * Payload is always the unmasked raw bytes.
 */
public record WebSocketFrame(boolean fin, int opcode, byte[] payload) {

    public static final int OP_TEXT = 0x1;
    public static final int OP_CLOSE = 0x8;

    public static WebSocketFrame text(String text) {
        return new WebSocketFrame(true, OP_TEXT, text.getBytes(StandardCharsets.UTF_8));
    }

    public static WebSocketFrame close(int statusCode) {
        byte[] payload = new byte[]{
                (byte) ((statusCode >> 8) & 0xFF), (byte) (statusCode & 0xFF)
        };
        return new WebSocketFrame(true, OP_CLOSE, payload);
    }
}
