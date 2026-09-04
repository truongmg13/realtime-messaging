package com.truongmg.messaging.controller;

import com.truongmg.messaging.dto.UserResponse;
import com.truongmg.messaging.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public List<UserResponse> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UUID userId) {
        return userService.search(userId, query, limit);
    }

}
