package com.truongmg.messaging.service;

import com.truongmg.messaging.dto.AuthResponse;
import com.truongmg.messaging.dto.RegisterRequest;
import com.truongmg.messaging.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public AuthResponse register(RegisterRequest request) {
        return null;
    }

}
