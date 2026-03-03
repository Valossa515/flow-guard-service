package io.github.valossa515.flow_guard_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRateLimitRuleRequest {

    @NotBlank(message = "clientId is required")
    private String clientId;

    @NotBlank(message = "endpoint is required")
    private String endpoint;

    @NotBlank(message = "httpMethod is required")
    private String httpMethod;

    @Min(value = 1, message = "limit must be at least 1")
    private long limit;

    @Min(value = 1, message = "windowSeconds must be at least 1")
    private long windowSeconds;
}
