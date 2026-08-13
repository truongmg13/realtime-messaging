package com.truongmg.messaging.dto;

import java.util.UUID;

public record AuthResponse(String token, UUID userId, String username, String displayName) { }
