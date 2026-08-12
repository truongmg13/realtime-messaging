package com.truongmg.messaging.websocket;

import com.truongmg.messaging.handshake.HandShakeParser;
import com.truongmg.messaging.handshake.HandShakeResponder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Manages full lifecycle of a single WebSocket Connection
 *
 * State machine:
 *      HANDSHAKING -> AUTHENTICATING -> OPEN -> CLOSED
 */
@Slf4j
public class WebSocketConnection implements Runnable {

    private final Socket socket;
    private OutputStream out;

    public WebSocketConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // perform handshake
        try {
            performHandshake();
        } catch (Exception e) {
            log.error("Error occurred while handling WebSocket connection: {}", e.getMessage(), e);
        }
    }

    private void performHandshake() throws IOException {
        log.info("Handshake request received");
        InputStream in = socket.getInputStream();
        out = socket.getOutputStream();

        HandShakeParser request;
        try {
            request = HandShakeParser.parse(in);
        } catch (IllegalArgumentException e) {
            // write to output stream to response back to client
            throw new IOException("Rejected non-WebSocket request: " + e.getMessage());
        }

        HandShakeResponder.respond(request, out);

        log.info("WebSocket handshake completed successfully");
    }

}
