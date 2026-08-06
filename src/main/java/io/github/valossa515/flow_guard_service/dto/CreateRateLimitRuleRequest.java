package io.github.valossa515.flow_guard_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRateLimitRuleRequest {
    @NotBlank
    private String clientId;

    @NotBlank
    private String endpoint;

    @NotBlank
    private String httpMethod;

    @Min(1)
    private long limit;

    @Min(1)
    private long windowSeconds;
}
