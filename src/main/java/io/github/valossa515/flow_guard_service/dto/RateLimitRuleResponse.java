package io.github.valossa515.flow_guard_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RateLimitRuleResponse {
    private String clientId;
    private String endpoint;
    private String httpMethod;
    private long limit;
    private long windowSeconds;

    public static RateLimitRuleResponse from(RateLimitRule rule) {
        RateLimitRuleResponse resp = new RateLimitRuleResponse();
        resp.setClientId(rule.getClientId());
        resp.setEndpoint(rule.getEndpoint());
        resp.setHttpMethod(rule.getHttpMethod());
        resp.setLimit(rule.getLimit());
        resp.setWindowSeconds(rule.getWindowSeconds());
        return resp;
    }
}
