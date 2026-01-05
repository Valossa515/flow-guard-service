package io.github.valossa515.flow_guard_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RateLimitRequestDTO {
    @NotBlank
    private String clientId;

    @NotBlank
    private String endpoint;

    @NotBlank
    private String httpMethod;
}
