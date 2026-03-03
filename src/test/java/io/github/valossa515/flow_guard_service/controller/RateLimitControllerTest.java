package io.github.valossa515.flow_guard_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateLimitController.class)
class RateLimitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RateLimitService rateLimitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnOkWhenRequestIsValid() throws Exception {
        RateLimitResponseDTO response = new RateLimitResponseDTO(
                true, DecisionStatus.ALLOWED.name(), 99, 60);

        when(rateLimitService.checkRateLimit(any())).thenReturn(response);

        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("POST");

        mockMvc.perform(post("/api/v1/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.decision").value("ALLOWED"))
                .andExpect(jsonPath("$.remainingRequests").value(99));
    }

    @Test
    void shouldReturnBadRequestWhenClientIdMissing() throws Exception {
        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setEndpoint("/api/test");
        request.setHttpMethod("GET");

        mockMvc.perform(post("/api/v1/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenHttpMethodInvalid() throws Exception {
        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("INVALID");

        mockMvc.perform(post("/api/v1/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBlockedResponse() throws Exception {
        RateLimitResponseDTO response = new RateLimitResponseDTO(
                false, DecisionStatus.RATE_LIMIT_EXCEEDED.name(), 0, 45);

        when(rateLimitService.checkRateLimit(any())).thenReturn(response);

        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("GET");

        mockMvc.perform(post("/api/v1/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.decision").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.remainingRequests").value(0));
    }
}
