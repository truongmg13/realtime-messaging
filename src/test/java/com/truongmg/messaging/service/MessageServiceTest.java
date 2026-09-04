package com.truongmg.messaging.service;

import com.truongmg.messaging.dto.ConversationPageResponse;
import com.truongmg.messaging.exception.NotFoundException;
import com.truongmg.messaging.model.Message;
import com.truongmg.messaging.model.MessageStatus;
import com.truongmg.messaging.model.User;
import com.truongmg.messaging.repository.MessageRepository;
import com.truongmg.messaging.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private UserRepository userRepository;

    private MessageService messageService;
    private UUID userId;
    private UUID otherUserId;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageRepository, userRepository);
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
    }

    @Test
    void getConversation_otherUserExists_returnsMappedPage() {
        Message message = buildMessage(userId, otherUserId, "hi there");
        when(userRepository.existsById(otherUserId)).thenReturn(true);
        when(messageRepository.findConversation(eq(userId), eq(otherUserId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(message)));

        ConversationPageResponse response = messageService.getConversation(userId, otherUserId, 0, 50);

        assertThat(response.messages()).hasSize(1);
        assertThat(response.messages().getFirst().content()).isEqualTo("hi there");
        assertThat(response.messages().getFirst().senderId()).isEqualTo(userId);
        assertThat(response.messages().getFirst().recipientId()).isEqualTo(otherUserId);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(50);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.hasMore()).isFalse();
    }

    @Test
    void getConversation_otherUserMissing_throwsNotFoundWithoutQueryingMessages() {
        when(userRepository.existsById(otherUserId)).thenReturn(false);

        assertThatThrownBy(() -> messageService.getConversation(userId, otherUserId, 0, 50))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(messageRepository);
    }

    private Message buildMessage(UUID senderId, UUID recipientId, String content) {
        User sender = new User();
        setId(sender, senderId);
        User recipient = new User();
        setId(recipient, recipientId);

        Message message = new Message();
        setId(message, UUID.randomUUID());
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setStatus(MessageStatus.SENT);
        setSentAt(message, Instant.now());
        return message;
    }

    private void setSentAt(Message message, Instant sentAt) {
        try {
            var field = Message.class.getDeclaredField("sentAt");
            field.setAccessible(true);
            field.set(message, sentAt);
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
