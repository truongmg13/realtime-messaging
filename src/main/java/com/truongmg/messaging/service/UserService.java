package com.truongmg.messaging.service;

import com.truongmg.messaging.dto.UserResponse;
import com.truongmg.messaging.exception.BadRequestException;
import com.truongmg.messaging.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_RESULTS = 50;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> search(UUID currentUserId, String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Search query must not be blank");
        }

        Pageable pageable = PageRequest.of(0, Math.clamp(limit, 1, MAX_RESULTS));
        return userRepository.search(query.trim(), currentUserId, pageable)
                .stream()
                .map(UserResponse::from)
                .toList();
    }
}
