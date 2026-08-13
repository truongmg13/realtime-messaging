package com.truongmg.messaging.service;

import com.truongmg.messaging.dto.AuthResponse;
import com.truongmg.messaging.dto.LoginRequest;
import com.truongmg.messaging.dto.RegisterRequest;
import com.truongmg.messaging.exception.ConflictException;
import com.truongmg.messaging.exception.UnauthorizedException;
import com.truongmg.messaging.model.User;
import com.truongmg.messaging.repository.UserRepository;
import com.truongmg.messaging.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        String username = request.username();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already taken: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user = userRepository.save(user);

        log.info("New user registered: {}, ({})", username, user.getId());
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        log.info("User logged in: {} ({})", user.getUsername(), user.getId());
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getDisplayName());
    }
}
