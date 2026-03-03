package io.github.valossa515.flow_guard_service.repository;

import io.github.valossa515.flow_guard_service.config.RedisKeyConstants;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class RateLimitRuleRedisRepository {

    private static final Logger log = LoggerFactory.getLogger(RateLimitRuleRedisRepository.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitRuleRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(RateLimitRule rule) {
        String key = RedisKeyConstants.buildRuleKey(
                rule.getClientId(), rule.getEndpoint(), rule.getHttpMethod());

        redisTemplate.opsForHash().put(key, "limit", String.valueOf(rule.getLimit()));
        redisTemplate.opsForHash().put(key, "window", String.valueOf(rule.getWindowSeconds()));

        log.info("Saved rate limit rule: clientId={}, endpoint={}, method={}, limit={}, window={}s",
                rule.getClientId(), rule.getEndpoint(), rule.getHttpMethod(),
                rule.getLimit(), rule.getWindowSeconds());
    }

    public Optional<RateLimitRule> find(String clientId, String endpoint, String method) {
        String key = RedisKeyConstants.buildRuleKey(clientId, endpoint, method);
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);

        if (data.isEmpty()) {
            return Optional.empty();
        }

        Object limitValue = data.get("limit");
        Object windowValue = data.get("window");

        if (limitValue == null || windowValue == null) {
            log.warn("Incomplete rule data in Redis for key={}: limit={}, window={}",
                    key, limitValue, windowValue);
            return Optional.empty();
        }

        RateLimitRule rule = new RateLimitRule();
        rule.setClientId(clientId);
        rule.setEndpoint(endpoint);
        rule.setHttpMethod(method);
        rule.setLimit(Long.parseLong(limitValue.toString()));
        rule.setWindowSeconds(Long.parseLong(windowValue.toString()));

        return Optional.of(rule);
    }

    public void delete(String clientId, String endpoint, String method) {
        String key = RedisKeyConstants.buildRuleKey(clientId, endpoint, method);
        redisTemplate.delete(key);

        log.info("Deleted rate limit rule: clientId={}, endpoint={}, method={}",
                clientId, endpoint, method);
    }
}
