package com.truongmg.messaging.frame;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * Represents a single decoded WebSocket frame per RFC 6455 Section 5.
 * Payload is always the unmasked raw bytes.
 */
@Getter
public class WebSocketFrame {

    public static final int OP_TEXT = 0x1;

    private final boolean fin;
    private final int opcode;
    private final byte[] payload;

    public WebSocketFrame(boolean fin, int opcode, byte[] payload) {
        this.fin = fin;
        this.opcode = opcode;
        this.payload = payload;
    }


    public static WebSocketFrame text(String text) {
        return new WebSocketFrame(true, OP_TEXT, text.getBytes(StandardCharsets.UTF_8));
    }
}
