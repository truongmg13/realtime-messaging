package com.truongmg.messaging.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false, length = 100)
    private String displayName;
    
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    private void prePersist() { this.createdAt = Instant.now(); }
    
}
