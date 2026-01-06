package io.github.valossa515.flow_guard_service.repository;

import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class RateLimitRuleRedisRepository {
    private final StringRedisTemplate redisTemplate;

    public RateLimitRuleRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(RateLimitRule rule) {
        String key = buildKey(rule.getClientId(), rule.getEndpoint(), rule.getHttpMethod());

        redisTemplate.opsForHash().put(key, "limit", String.valueOf(rule.getLimit()));
        redisTemplate.opsForHash().put(key, "window", String.valueOf(rule.getWindowSeconds()));
    }

    public Optional<RateLimitRule> find(String clientId, String endpoint, String method) {

        String key = buildKey(clientId, endpoint, method);
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);

        if (data.isEmpty()) {
            return Optional.empty();
        }

        RateLimitRule rule = new RateLimitRule();
        rule.setClientId(clientId);
        rule.setEndpoint(endpoint);
        rule.setHttpMethod(method);
        rule.setLimit(Long.parseLong(data.get("limit").toString()));
        rule.setWindowSeconds(Long.parseLong(data.get("window").toString()));

        return Optional.of(rule);
    }

    private String buildKey(String clientId, String endpoint, String method) {
        return "rate-limit:rule:" + clientId + ":" + endpoint + ":" + method;
    }
}