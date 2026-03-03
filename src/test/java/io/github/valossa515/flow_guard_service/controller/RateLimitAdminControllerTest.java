package io.github.valossa515.flow_guard_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.valossa515.flow_guard_service.dto.CreateRateLimitRuleRequest;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.service.RateLimitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateLimitAdminController.class)
class RateLimitAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RateLimitAdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateRuleAndReturn201() throws Exception {
        CreateRateLimitRuleRequest request = new CreateRateLimitRuleRequest();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("POST");
        request.setLimit(50);
        request.setWindowSeconds(120);

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(adminService).create(any(CreateRateLimitRuleRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsZero() throws Exception {
        CreateRateLimitRuleRequest request = new CreateRateLimitRuleRequest();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("GET");
        request.setLimit(0);
        request.setWindowSeconds(60);

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenHttpMethodInvalid() throws Exception {
        CreateRateLimitRuleRequest request = new CreateRateLimitRuleRequest();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("INVALID");
        request.setLimit(10);
        request.setWindowSeconds(60);

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetRuleAndReturn200() throws Exception {
        RateLimitRule rule = new RateLimitRule("client-1", "/api/test", "GET", 10, 60);
        when(adminService.get("client-1", "/api/test", "GET"))
                .thenReturn(Optional.of(rule));

        mockMvc.perform(get("/api/v1/rules")
                        .param("clientId", "client-1")
                        .param("endpoint", "/api/test")
                        .param("method", "GET"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("client-1"))
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    void shouldReturn404WhenRuleNotFound() throws Exception {
        when(adminService.get("unknown", "/api/test", "GET"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/rules")
                        .param("clientId", "unknown")
                        .param("endpoint", "/api/test")
                        .param("method", "GET"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteRuleAndReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/rules")
                        .param("clientId", "client-1")
                        .param("endpoint", "/api/test")
                        .param("method", "POST"))
                .andExpect(status().isNoContent());

        verify(adminService).delete("client-1", "/api/test", "POST");
    }
}
