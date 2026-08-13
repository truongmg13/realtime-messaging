package com.truongmg.messaging.frame;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Encodes a WebSocketFrame to raw bytes per RFC 6455 section 5.2
 *
 * Server -> Client frames are NEVER masked (section 5.1)
 * Support 7-bit, 16-bit, and 64-bit payload lengths
 */
public class FrameEncoder {

    /**
     * Writes the frame to the OutputStream and flushes
     *
     */
    public void encode(WebSocketFrame frame, OutputStream out) throws IOException {
        byte[] payload = frame.payload();

        // Byte 0: FIN=1, RSV1-3=0, Opcode
        out.write(0x80 | (frame.opcode() & 0x0F));

        // Byte 1 onward: MASK=0, then payload length
        int len = payload.length;
        if (len < 126) {
            out.write(len);
        } else if (len < 65536) {
            out.write(126);
            out.write((len >> 8) & 0xFF);
            out.write(len & 0xFF);
        } else {
            out.write(127);
            for (int i = 7; i >= 0; i--) {
                out.write((int) (((long) len >> (i * 8)) & 0xFF));
            }
        }

        out.write(payload);
        out.flush();
    }
}
