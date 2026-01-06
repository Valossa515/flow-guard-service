package io.github.valossa515.flow_guard_service.service.impl;

import io.github.valossa515.flow_guard_service.config.DefaultRateLimitProperties;
import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.service.RateLimitRuleResolver;
import io.github.valossa515.flow_guard_service.service.RateLimitService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitRedisServiceImpl implements RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitRuleResolver ruleResolver;

    public RateLimitRedisServiceImpl(StringRedisTemplate redisTemplate,
                                     RateLimitRuleResolver ruleResolver) {
        this.redisTemplate = redisTemplate;
        this.ruleResolver = ruleResolver;
    }

    @Override
    public RateLimitResponseDTO checkRateLimit(RateLimitRequestDTO request) {

        RateLimitRule rule = ruleResolver.resolve(request);
        String key = buildKey(request);

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(rule.getWindowSeconds()));
        }

        Long ttl = redisTemplate.getExpire(key);

        if (count != null && count > rule.getLimit()) {
            return new RateLimitResponseDTO(
                    false,
                    DecisionStatus.RATE_LIMIT_EXCEEDED.name(),
                    0,
                    ttl != null ? ttl : rule.getWindowSeconds()
            );
        }

        return new RateLimitResponseDTO(
                true,
                DecisionStatus.ALLOWED.name(),
                rule.getLimit() - count,
                ttl != null ? ttl : rule.getWindowSeconds()
        );
    }

    private String buildKey(RateLimitRequestDTO request) {
        return "rate-limit:"
                + request.getClientId()
                + ":" + request.getEndpoint()
                + ":" + request.getHttpMethod();
    }
}