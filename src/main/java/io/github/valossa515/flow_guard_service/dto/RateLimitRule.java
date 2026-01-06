package io.github.valossa515.flow_guard_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitRule {
    private String clientId;
    private String endpoint;
    private String httpMethod;
    private long limit;
    private long windowSeconds;
}
