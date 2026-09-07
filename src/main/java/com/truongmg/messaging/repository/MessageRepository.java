package com.truongmg.messaging.repository;

import com.truongmg.messaging.model.Message;
import com.truongmg.messaging.model.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Messages that have been persisted but not yet delivered to the recipient
     * (recipient was offline at the same time)
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.recipient.id = :recipientId AND m.status = :status
        ORDER BY m.sentAt ASC
        """)
    List<Message> findByRecipientIdAndStatus(@Param("recipientId") UUID recipientId,
                                             @Param("status") MessageStatus status);

    /**
     * All messages exchanged between two users, in either direction.
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        JOIN FETCH m.recipient
        WHERE (m.sender.id = :userId1 AND m.recipient.id = :userId2)
           OR (m.sender.id = :userId2 AND m.recipient.id = :userId1)
        """)
    Page<Message> findConversation(@Param("userId1") UUID userId1,
                                    @Param("userId2") UUID userId2,
                                    Pageable pageable);

}
