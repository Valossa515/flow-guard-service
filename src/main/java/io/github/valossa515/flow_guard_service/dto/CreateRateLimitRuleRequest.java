package io.github.valossa515.flow_guard_service.dto;

import lombok.Data;

@Data
public class CreateRateLimitRuleRequest {
    private String clientId;
    private String endpoint;
    private String httpMethod;

    private long limit;
    private long windowSeconds;
}
