package com.truongmg.messaging.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truongmg.messaging.model.Message;
import com.truongmg.messaging.model.MessageStatus;
import com.truongmg.messaging.model.User;
import com.truongmg.messaging.service.MessageService;
import com.truongmg.messaging.websocket.routing.MessageRouter;
import com.truongmg.messaging.websocket.session.SessionRegistry;
import com.truongmg.messaging.websocket.session.WebSocketSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageRouterTest {

    @Mock private SessionRegistry sessionRegistry;
    @Mock private MessageService messageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MessageRouter router;

    @BeforeEach
    void setUp() {
        router = new MessageRouter(sessionRegistry, messageService, objectMapper);
    }

    @Test
    void route_recipientOnline_deliversAndMarksDelivered() throws IOException {
        UUID recipientId = UUID.randomUUID();
        Message message = buildMessage(recipientId);

        List<String> sentFrames = new ArrayList<>();
        WebSocketSession session = new WebSocketSession(recipientId, sentFrames::add);
        when(sessionRegistry.find(recipientId)).thenReturn(Optional.of(session));

        router.route(message);

        assertThat(sentFrames).hasSize(1);
        assertThat(sentFrames.getFirst()).contains("\"type\":\"MESSAGE\"");
        assertThat(sentFrames.getFirst()).contains("\"content\":\"Hello!\"");
        assertThat(sentFrames.getFirst()).contains(message.getId().toString());
        verify(messageService).markDelivered(message.getId());
    }

    @Test
    void route_recipientOffline_doesNotDeliver() {
        UUID recipientId = UUID.randomUUID();
        Message message = buildMessage(recipientId);

        when(sessionRegistry.find(recipientId)).thenReturn(Optional.empty());

        router.route(message);

        verifyNoInteractions(messageService);
    }

    @Test
    void deliverNow_ioExceptionOnSend_doesNotMarkDelivered() throws IOException {
        UUID recipientId = UUID.randomUUID();
        Message message = buildMessage(recipientId);

        WebSocketSession.WebSocketSender failingSender = json -> { throw new IOException("socket closed"); };
        WebSocketSession session = new WebSocketSession(recipientId, failingSender);

        router.deliverNow(session, message);

        verify(messageService, never()).markDelivered(any());
    }

    private Message buildMessage(UUID recipientId) {
        User sender = new User();
        sender.setUsername("alice");
        sender.setDisplayName("Alice");
        setId(sender, UUID.randomUUID());

        User recipient = new User();
        recipient.setUsername("bob");
        recipient.setDisplayName("Bob");
        setId(recipient, recipientId);

        Message msg = new Message();
        setId(msg, UUID.randomUUID());
        msg.setSender(sender);
        msg.setRecipient(recipient);
        msg.setContent("Hello!");
        msg.setStatus(MessageStatus.SENT);
        setSentAt(msg, Instant.now());

        return msg;
    }

    private void setSentAt(Message msg, Instant sentAt) {
        try {
            var field = Message.class.getDeclaredField("sentAt");
            field.setAccessible(true);
            field.set(msg, sentAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
