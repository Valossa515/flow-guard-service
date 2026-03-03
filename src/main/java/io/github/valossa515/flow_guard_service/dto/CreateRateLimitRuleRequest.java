package io.github.valossa515.flow_guard_service.dto;

import io.github.valossa515.flow_guard_service.validation.ValidHttpMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRateLimitRuleRequest {

    @NotBlank(message = "clientId is required")
    @Size(max = 128, message = "clientId must be at most 128 characters")
    private String clientId;

    @NotBlank(message = "endpoint is required")
    @Size(max = 256, message = "endpoint must be at most 256 characters")
    private String endpoint;

    @ValidHttpMethod
    private String httpMethod;

    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 1_000_000, message = "limit must be at most 1000000")
    private long limit;

    @Min(value = 1, message = "windowSeconds must be at least 1")
    @Max(value = 86400, message = "windowSeconds must be at most 86400 (24h)")
    private long windowSeconds;
}
