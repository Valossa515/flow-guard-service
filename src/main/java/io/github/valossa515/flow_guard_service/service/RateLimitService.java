package io.github.valossa515.flow_guard_service.service;

import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;

public interface RateLimitService {
    RateLimitResponseDTO checkRateLimit(RateLimitRequestDTO request);
}