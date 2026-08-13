package com.truongmg.messaging.service;

import com.truongmg.messaging.exception.NotFoundException;
import com.truongmg.messaging.model.Message;
import com.truongmg.messaging.model.User;
import com.truongmg.messaging.repository.MessageRepository;
import com.truongmg.messaging.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles message persistence only.
 * Routing (live delivery via WebSocket) is orchestrated by ProtocolHandler
 * after calling save(), keeping this service framework-agnostic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Message save(UUID senderId, UUID recipientId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Sender not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        return messageRepository.save(message);
    }

}
