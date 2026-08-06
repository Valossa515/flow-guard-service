package io.github.valossa515.flow_guard_service.repository;

import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class RateLimitRuleRedisRepository {

    private static final String RULE_KEY_PREFIX = "rate-limit:rule:";

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

    public List<RateLimitRule> findAll() {
        List<String> keys = new ArrayList<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(RULE_KEY_PREFIX + "*")
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }

        List<RateLimitRule> rules = new ArrayList<>();
        for (String key : keys) {
            parseKey(key).ifPresent(rule ->
                    find(rule.getClientId(), rule.getEndpoint(), rule.getHttpMethod())
                            .ifPresent(rules::add));
        }
        return rules;
    }

    /**
     * Chave no formato rate-limit:rule:{clientId}:{endpoint}:{method}.
     * O clientId não contém ':' e o método é sempre o último segmento,
     * então o endpoint fica entre o primeiro e o último ':' do restante.
     */
    private Optional<RateLimitRule> parseKey(String key) {
        String raw = key.substring(RULE_KEY_PREFIX.length());

        int firstColon = raw.indexOf(':');
        int lastColon = raw.lastIndexOf(':');
        if (firstColon < 0 || lastColon <= firstColon) {
            return Optional.empty();
        }

        RateLimitRule rule = new RateLimitRule();
        rule.setClientId(raw.substring(0, firstColon));
        rule.setEndpoint(raw.substring(firstColon + 1, lastColon));
        rule.setHttpMethod(raw.substring(lastColon + 1));
        return Optional.of(rule);
    }

    private String buildKey(String clientId, String endpoint, String method) {
        return RULE_KEY_PREFIX + clientId + ":" + endpoint + ":" + method;
    }

    public void delete(String clientId, String endpoint, String method) {
        redisTemplate.delete(buildKey(clientId, endpoint, method));
    }
}