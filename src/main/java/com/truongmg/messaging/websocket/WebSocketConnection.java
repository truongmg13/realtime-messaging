package com.truongmg.messaging.websocket;

import com.truongmg.messaging.frame.FrameDecoder;
import com.truongmg.messaging.frame.FrameEncoder;
import com.truongmg.messaging.frame.WebSocketFrame;
import com.truongmg.messaging.handshake.HandShakeParser;
import com.truongmg.messaging.handshake.HandShakeResponder;
import com.truongmg.messaging.protocol.ProtocolHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
    private final ProtocolHandler protocolHandler;
    private final long authTimeout;

    private final FrameDecoder frameDecoder = new FrameDecoder();
    private final FrameEncoder frameEncoder = new FrameEncoder();

    private State state = State.HANDSHAKING;
    private UUID authenticatedUserId;
    private OutputStream out;

    WebSocketConnection(Socket socket, ProtocolHandler protocolHandler, long authTimeout) {
        this.socket = socket;
        this.protocolHandler = protocolHandler;
        this.authTimeout = authTimeout;
    }

    @Override
    public void run() {
        try {
            performHandshake();
            runReadLoop();
        } catch (Exception e) {
            log.error("Unexpected error in connection ({}): {}", remoteAddr(), e.getMessage(), e);
        } finally {
            close();
        }
    }

    private void runReadLoop() throws IOException {
        InputStream in = socket.getInputStream();
        while (state != State.CLOSED) {
            WebSocketFrame frame = frameDecoder.decode(in);
            handleFrame(frame);
        }
    }

    private void handleFrame(WebSocketFrame frame) {
        // Only support Text as of now
        switch (frame.opcode()) {
            case WebSocketFrame.OP_TEXT -> handleText(frame);
            default -> log.warn("unknown opcode ox{} from {}", Integer.toHexString(frame.opcode()), remoteAddr());
        }
    }

    private void handleText(WebSocketFrame frame) {
        if (state == State.CLOSED) return;
        String json = new String(frame.payload(), StandardCharsets.UTF_8);
        log.info("received text frame from {}: {}", remoteAddr(), json);
        protocolHandler.handleMessage(this, json);
    }

    private void performHandshake() throws IOException {
        log.info("Handshake request received");
        InputStream in = socket.getInputStream();
        out = socket.getOutputStream();

        HandShakeParser request;
        try {
            request = HandShakeParser.parse(in);
        } catch (IllegalArgumentException e) {
            out.write(("HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\n\r\n")
                    .getBytes(StandardCharsets.US_ASCII));
            out.flush();
            // write to output stream to response back to client
            throw new IOException("Rejected non-WebSocket request: " + e.getMessage());
        }

        HandShakeResponder.respond(request, out);
        state = State.AUTHENTICATING;

        log.info("WebSocket handshake completed successfully from {}", remoteAddr());
        scheduleAuthTimeout();
    }

    private void scheduleAuthTimeout() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (state == State.AUTHENTICATING) {
                    log.warn("Authentication timed out from {}", remoteAddr());
                    try { sendFrame(WebSocketFrame.close(4001)); } catch (IOException ignored) {}
                    close();
                }

            }
        }, authTimeout);
    }

    private void sendFrame(WebSocketFrame frame) throws IOException {
        if (out != null) {
            frameEncoder.encode(frame, out);
        }
    }

    public void send(String json) throws IOException {
        // encode before sending response to client
        frameEncoder.encode(WebSocketFrame.text(json), out);
    }

    private void close() {
        // TODO: close the connection
        try { socket.close(); } catch (IOException ignored) {}
    }

    private String remoteAddr() {
        return socket.isConnected() ? socket.getRemoteSocketAddress().toString() : "unknown";
    }

    // -- Public API -----------
    public void updateAuthDetails(UUID userId) {
        this.authenticatedUserId = userId;
        this.state = State.OPEN;
    }

    public boolean isAuthenticated() {
        return state == State.OPEN && authenticatedUserId != null;
    }

}
