package io.github.valossa515.flow_guard_service.service.impl;

import io.github.valossa515.flow_guard_service.config.DefaultRateLimitProperties;
import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.service.RateLimitService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitRedisServiceImpl implements RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRateLimitProperties defaultRule;

    public RateLimitRedisServiceImpl(StringRedisTemplate redisTemplate,
                                     DefaultRateLimitProperties defaultRule) {
        this.defaultRule = defaultRule;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitResponseDTO checkRateLimit(RateLimitRequestDTO request) {
        String key = buildKey(request);
        long limit = defaultRule.getLimit();
        long windowSeconds = defaultRule.getWindowSeconds();

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        Long ttl = redisTemplate.getExpire(key);

        if (currentCount != null && currentCount > limit) {
            return new RateLimitResponseDTO(
                    false,
                    DecisionStatus.RATE_LIMIT_EXCEEDED.name(),
                    0,
                    ttl != null ? ttl : windowSeconds
            );
        }

        long remaining = limit - (currentCount != null ? currentCount : 0);

        return new RateLimitResponseDTO(
                true,
                DecisionStatus.ALLOWED.name(),
                remaining,
                ttl != null ? ttl : windowSeconds
        );
    }

    private String buildKey(RateLimitRequestDTO request) {
        return "rate-limit:"
                + request.getClientId()
                + ":" + request.getEndpoint()
                + ":" + request.getHttpMethod();
    }
}