package com.truongmg.messaging.websocket.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truongmg.messaging.model.Message;
import com.truongmg.messaging.service.MessageService;
import com.truongmg.messaging.websocket.session.SessionRegistry;
import com.truongmg.messaging.websocket.session.WebSocketSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Deliver a persisted message to recipient if he is online
 * If offline, message stays in SENT state and will be pushed in the next AUTH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRouting {

    private final SessionRegistry sessionRegistry;
    private final MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Attempts live delivery. Called after the message is already persisted.
     */
    public void route(Message message) {
        UUID recipientId = message.getRecipient().getId();
        sessionRegistry.find(recipientId).ifPresentOrElse(
                session -> deliverNow(session, message),
                () -> log.debug("Recipient {} offline - message {} queued", recipientId, message)
        );
    }

    /**
     * Push a message to an already-located session and update its status
     */
    private void deliverNow(WebSocketSession session, Message message) {

    }

}
