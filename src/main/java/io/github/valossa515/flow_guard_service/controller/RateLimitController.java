package io.github.valossa515.flow_guard_service.controller;

import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.service.RateLimitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rate-limit")
public class RateLimitController {

    private final RateLimitService rateLimitService;

    public RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/check")
    public ResponseEntity<RateLimitResponseDTO> checkRateLimit(
            @Valid @RequestBody RateLimitRequestDTO request) {

        RateLimitResponseDTO response = rateLimitService.checkRateLimit(request);
        return ResponseEntity.ok(response);
    }
}