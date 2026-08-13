package com.truongmg.messaging.frame;

import lombok.Getter;

/**
 * Represents a single decoded WebSocket frame per RFC 6455 Section 5.
 * Payload is always the unmasked raw bytes.
 */
@Getter
public class WebSocketFrame {

    private final boolean fin;
    private final int opcode;
    private final byte[] payload;

    public WebSocketFrame(boolean fin, int opcode, byte[] payload) {
        this.fin = fin;
        this.opcode = opcode;
        this.payload = payload;
    }


}
