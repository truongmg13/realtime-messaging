package com.truongmg.messaging.frame;

import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.IOException;
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
@Slf4j
public class FrameDecoder {

    public WebSocketFrame decode(InputStream in) throws IOException {
        int byte0 = readByte(in);
        boolean fin = (byte0 & 0x80) != 0;
        int opcode = byte0 & 0x0F;

        int byte1 = readByte(in);
        boolean masked = (byte1 & 0x80) != 0;
        long payloadLength = byte1 & 0x7F;

        if (payloadLength == 126) {
            // Next 2 bytes are 16-bit unsigned length
            payloadLength = ((long) readByte(in) << 8) | readByte(in);
        } else if (payloadLength == 127) {
            // Next 8 bytes are 64-bit unsigned length
            payloadLength = 0;
            for (int i = 0; i < 8; i++) {
                payloadLength = (payloadLength << 8) | readByte(in);
            }
        }

        byte[] maskKey = null;
        if (masked) {
            maskKey = readBytes(in, 4);
        }

        byte[] payload = readBytes(in, (int) payloadLength);

        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] ^= maskKey[i % 4];
            }
        }

        return new WebSocketFrame(fin, opcode, payload);
    }

    private int readByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) throw new EOFException("Stream closed");
        return b;
    }

    private byte[] readBytes(InputStream in, int count) throws IOException {
        if (count == 0) return new byte[0];
        byte[] buf = new byte[count];
        int read = 0;
        while (read < count) {
            int n = in.read(buf, read, count - read);
            if (n == -1) throw new EOFException("Unexpected end of stream");
            read += n;
        }
        return buf;
    }

}
