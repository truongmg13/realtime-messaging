package com.truongmg.messaging.websocket;

import lombok.extern.slf4j.Slf4j;

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

    public WebSocketConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
