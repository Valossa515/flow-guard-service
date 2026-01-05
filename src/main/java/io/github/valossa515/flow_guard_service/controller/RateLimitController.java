package io.github.valossa515.flow_guard_service.controller;

import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rate-limit")
public class RateLimitController {

    @PostMapping("/check")
    public ResponseEntity<RateLimitResponseDTO> checkRateLimit(
            @Valid @RequestBody RateLimitRequestDTO request) {

        // MOCK temporário — será substituído pelo Service
        RateLimitResponseDTO response = new RateLimitResponseDTO(
                true,
                DecisionStatus.ALLOWED.name(),
                100,
                60
        );

        return ResponseEntity.ok(response);
    }
}