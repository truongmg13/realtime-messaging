package com.truongmg.messaging.dto;

import com.truongmg.messaging.model.User;

import java.util.UUID;

public record UserResponse(UUID id, String username, String displayName) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
