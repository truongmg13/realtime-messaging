package com.truongmg.messaging.protocol;

/**
 * Discriminator field for every JSON envelope exchanged over WebSocket.
 *
 * Client -> Server: AUTH, SEND, READ
 * Server -> Client: MESSAGE
 *
 */
public enum MessageType {

    /**
     * Send a message to another user.
     * Payload: { "type": "SEND", "recipientId": "<uuid>", "content": "<text>" }
     */
    SEND


}
