package io.github.valossa515.flow_guard_service.dto;

import io.github.valossa515.flow_guard_service.validation.ValidHttpMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RateLimitRequestDTO {

    @NotBlank(message = "clientId is required")
    @Size(max = 128, message = "clientId must be at most 128 characters")
    private String clientId;

    @NotBlank(message = "endpoint is required")
    @Size(max = 256, message = "endpoint must be at most 256 characters")
    private String endpoint;

    @ValidHttpMethod
    private String httpMethod;
}
