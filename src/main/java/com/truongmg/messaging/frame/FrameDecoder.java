package com.truongmg.messaging.frame;

import java.io.InputStream;

/**
 * Decodes a WebSocket frame from a raw TCP InputStream per RFC 6455 Section 5.2
 *
 * Frame wire format:
 *   Byte 0: FIN(1) RSV1(1) RSV2(1) RSV3(1) Opcode(4)
 *   Byte 1: MASK(1) PayloadLen(7)
 *   [2 bytes] if PayloadLen == 126 -> 16-bit extended length
 *   [8 bytes] if PayloadLen == 127 -> 64-bit extended length
 *   [4 bytes] if MASK == 1         -> Masking key
 *   [PayloadLen bytes]             -> payload (XOR-unmasked if masked)
 *
 *   Client -> Server frames MUST be masked. Server -> Client frames MUST NOT be masked.
 *   This decoder handles both.
 */
public class FrameDecoder {

    public WebSocketFrame decode(InputStream in) {


        return new WebSocketFrame();
    }

}
