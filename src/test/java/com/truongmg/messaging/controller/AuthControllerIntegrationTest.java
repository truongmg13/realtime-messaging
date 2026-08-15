package com.truongmg.messaging.controller;

import com.truongmg.messaging.dto.LoginRequest;
import com.truongmg.messaging.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";

    @Test
    void register_success_returns201WithToken() throws Exception {
        var req = new RegisterRequest(
                "testuser_" + System.nanoTime(),
                "password123",
                "Test User"
        );

        mvc.perform(
                post(REGISTER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.username").value(req.username()));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        var username = "duplicate_" + System.nanoTime();
        var req = new RegisterRequest(
                username,
                "password123",
                "Dup User"
        );

        // First registration
        performRegisterUser(req, status().isCreated());

        // Second registration
        performRegisterUser(req, status().isConflict());
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        var username = "logintest_" +  System.nanoTime();
        var securePass = "password123";
        var registerRequest = new RegisterRequest(
                username,
                securePass,
                "Test User"
        );
        performRegisterUser(registerRequest, status().isCreated());

        var loginRequest = new LoginRequest(username, securePass);
        performLoginUser(loginRequest, status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        var username = "wrongpw_" +  System.nanoTime();
        var registerRequest = new RegisterRequest(
                username,
                "correctPass1",
                "Test User"
        );
        performRegisterUser(registerRequest, status().isCreated());

        var loginRequest = new LoginRequest(username, "wrongpw");
        performLoginUser(loginRequest, status().isUnauthorized());
    }

    private ResultActions performRegisterUser(RegisterRequest registerRequest, ResultMatcher matcher) throws Exception {
        return mvc.perform(
                    post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(matcher);
    }

    private ResultActions performLoginUser(LoginRequest loginRequest, ResultMatcher matcher) throws Exception {
        return mvc.perform(
                        post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(matcher);
    }
}