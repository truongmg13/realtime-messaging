package com.truongmg.messaging.controller;

import com.truongmg.messaging.dto.AuthResponse;
import com.truongmg.messaging.dto.RegisterRequest;
import com.truongmg.messaging.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MessageService messageService;

    @Test
    void getConversation_messagesExchanged_returnsHistoryNewestFirst() throws Exception {
        String suffix = System.nanoTime() + "";
        AuthResponse alice = register("alice_" + suffix, "Alice");
        AuthResponse bob = register("bob_" + suffix, "Bob");

        messageService.save(alice.userId(), bob.userId(), "first");
        messageService.save(bob.userId(), alice.userId(), "second");
        messageService.save(alice.userId(), bob.userId(), "third");

        mvc.perform(get("/api/messages/conversation/{otherUserId}", bob.userId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + alice.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages.length()").value(3))
                .andExpect(jsonPath("$.messages[0].content").value("third"))
                .andExpect(jsonPath("$.messages[2].content").value("first"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void getConversation_pagination_respectsPageAndSize() throws Exception {
        String suffix = System.nanoTime() + "";
        AuthResponse alice = register("alice2_" + suffix, "Alice");
        AuthResponse bob = register("bob2_" + suffix, "Bob");

        for (int i = 0; i < 3; i++) {
            messageService.save(alice.userId(), bob.userId(), "msg-" + i);
        }

        mvc.perform(get("/api/messages/conversation/{otherUserId}", bob.userId())
                        .param("page", "0")
                        .param("size", "2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + alice.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.hasMore").value(true));
    }

    @Test
    void getConversation_otherUserDoesNotExist_returns404() throws Exception {
        AuthResponse alice = register("alice3_" + System.nanoTime(), "Alice");

        mvc.perform(get("/api/messages/conversation/{otherUserId}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + alice.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getConversation_noAuth_returns403() throws Exception {
        mvc.perform(get("/api/messages/conversation/{otherUserId}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    private AuthResponse register(String username, String displayName) throws Exception {
        var req = new RegisterRequest(username, "password123", displayName);
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, AuthResponse.class);
    }
}
