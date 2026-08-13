package com.truongmg.messaging.handshake;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Computes the Sec-WebSocket-Accept key and write the HTTP 101 response
 * per RFC 6455 Section 4.2.2
 */
@Slf4j
public class HandShakeResponder {

    // Globally Unique Identifier
    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private static final String CRLF = "\r\n";

    /**
     * Computes the accept key and writes the 101 response
     * @param request
     * @param out
     */
    public static void respond(HandShakeParser request, OutputStream out) throws IOException {
        String acceptKey = computeAcceptKey(request.getWebSocketKey());

        String response = "HTTP/1.1 101 Switching Protocols" + CRLF
            + "Upgrade: websocket" + CRLF
            + "Connection: Upgrade" + CRLF
            + "Sec-WebSocket-Accept: " + acceptKey + CRLF
            + CRLF;

        log.info("Sending handshake response:\n{}", response);

        out.write(response.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    private static String computeAcceptKey(String clientKey) {
        try {
            String combined = clientKey + GUID;
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 algorithm not available", e);
        }
    }

}
