package com.truongmg.messaging.controller;

import com.truongmg.messaging.dto.AuthResponse;
import com.truongmg.messaging.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void search_matchingQuery_returnsOtherUserButExcludesSelf() throws Exception {
        String suffix = System.nanoTime() + "";
        AuthResponse self = register("searcher_" + suffix, "Searcher");
        AuthResponse target = register("target_" + suffix, "Findable Target " + suffix);

        mvc.perform(get("/api/users/search")
                        .param("q", "findable target " + suffix)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + self.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(target.userId().toString()))
                .andExpect(jsonPath("$[0].username").value(target.username()));
    }

    @Test
    void search_noAuth_returns403() throws Exception {
        mvc.perform(get("/api/users/search").param("q", "anything"))
                .andExpect(status().isForbidden());
    }

    @Test
    void search_blankQuery_returns400() throws Exception {
        AuthResponse self = register("blankq_" + System.nanoTime(), "Blank Query");

        mvc.perform(get("/api/users/search")
                        .param("q", "  ")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + self.token()))
                .andExpect(status().isBadRequest());
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
