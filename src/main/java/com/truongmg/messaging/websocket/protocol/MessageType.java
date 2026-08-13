package com.truongmg.messaging.websocket.protocol;

/**
 * Discriminator field for every JSON envelope exchanged over WebSocket.
 *
 * Client -> Server: AUTH, SEND, READ
 * Server -> Client: MESSAGE
 *
 */
public enum MessageType {

    /**
     * First message after handshake.
     * Payload: { "type": "AUTH", "token": "<jwt>" }
     */
    AUTH,

    /**
     * Send a message to another user.
     * Payload: { "type": "SEND", "recipientId": "<uuid>", "content": "<text>" }
     */
    SEND


}
