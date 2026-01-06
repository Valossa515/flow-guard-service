package io.github.valossa515.flow_guard_service.repository;

import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class RateLimitRuleRepository {

    private final Map<String, RateLimitRule> rules = new HashMap<>();

    public RateLimitRuleRepository() {
        // exemplos
        rules.put(
                key("client-123", "/payments", "POST"),
                new RateLimitRule("client-123", "/payments", "POST", 10, 60)
        );
    }

    public Optional<RateLimitRule> findRule(String clientId, String endpoint, String method) {
        return Optional.ofNullable(rules.get(key(clientId, endpoint, method)));
    }

    private String key(String clientId, String endpoint, String method) {
        return clientId + ":" + endpoint + ":" + method;
    }
}
