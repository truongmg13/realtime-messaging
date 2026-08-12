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

    private enum State { HANDSHAKING, AUTHENTICATING, OPEN, CLOSED }

    private final Socket socket;
    private OutputStream out;

    private State state = State.HANDSHAKING;

    public WebSocketConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            performHandshake();
            runReadLoop();
        } catch (Exception e) {
            log.error("Error occurred while handling WebSocket connection: {}", e.getMessage(), e);
        } finally {
            close();
        }
    }

    private void runReadLoop() throws IOException {
        InputStream in = socket.getInputStream();
        while (state != State.CLOSED) {
            // TODO: read and process WebSocket frames

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
        state = State.AUTHENTICATING;

        log.info("WebSocket handshake completed successfully from {}", remoteAddr());
    }

    private void close() {
        // TODO: close the connection
        try { socket.close(); } catch (IOException ignored) {}
    }

    private String remoteAddr() {
        return socket.isConnected() ? socket.getRemoteSocketAddress().toString() : "unknown";
    }

}
