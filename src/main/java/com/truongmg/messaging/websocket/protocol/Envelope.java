package com.truongmg.messaging.websocket.protocol;

/**
 * Flat JSON envelope for all client->server WebSocket messages.
 * Fields are optional depending on the message type
 *
 * Examples:
 *  {"type": "AUTH", "token": "<jtw>"}
 *  {"type": "SEND", "recipientId": "<uuid>", "content": "hello"}
 */
public record Envelope(
        MessageType type,
        String recipientId,
        String content,
        String token) { }
