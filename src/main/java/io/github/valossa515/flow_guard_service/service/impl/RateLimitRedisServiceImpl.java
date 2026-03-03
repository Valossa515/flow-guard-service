package io.github.valossa515.flow_guard_service.service.impl;

import io.github.valossa515.flow_guard_service.config.RedisKeyConstants;
import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.service.RateLimitRuleResolver;
import io.github.valossa515.flow_guard_service.service.RateLimitService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimitRedisServiceImpl implements RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitRedisServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final RateLimitRuleResolver ruleResolver;
    private final Counter checksCounter;
    private final Counter blockedCounter;

    private static final String RATE_LIMIT_LUA_SCRIPT =
            "local count = redis.call('INCR', KEYS[1]) " +
            "if count == 1 then " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "local ttl = redis.call('TTL', KEYS[1]) " +
            "return {count, ttl}";

    private final DefaultRedisScript<List> rateLimitScript;

    public RateLimitRedisServiceImpl(StringRedisTemplate redisTemplate,
                                     RateLimitRuleResolver ruleResolver,
                                     MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.ruleResolver = ruleResolver;

        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptText(RATE_LIMIT_LUA_SCRIPT);
        this.rateLimitScript.setResultType(List.class);

        this.checksCounter = Counter.builder("rate_limit.checks")
                .description("Total rate limit checks performed")
                .register(meterRegistry);
        this.blockedCounter = Counter.builder("rate_limit.blocked")
                .description("Total requests blocked by rate limiting")
                .register(meterRegistry);
    }

    @Override
    public RateLimitResponseDTO checkRateLimit(RateLimitRequestDTO request) {
        checksCounter.increment();

        RateLimitRule rule = ruleResolver.resolve(request);
        String key = RedisKeyConstants.buildCounterKey(
                request.getClientId(), request.getEndpoint(), request.getHttpMethod());

        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(
                rateLimitScript,
                List.of(key),
                String.valueOf(rule.getWindowSeconds())
        );

        long count = result != null ? result.get(0) : 0;
        long ttl = result != null ? result.get(1) : rule.getWindowSeconds();

        if (ttl < 0) {
            ttl = rule.getWindowSeconds();
        }

        if (count > rule.getLimit()) {
            blockedCounter.increment();

            log.info("Rate limit exceeded: clientId={}, endpoint={}, method={}, count={}, limit={}",
                    request.getClientId(), request.getEndpoint(), request.getHttpMethod(),
                    count, rule.getLimit());

            return new RateLimitResponseDTO(
                    false,
                    DecisionStatus.RATE_LIMIT_EXCEEDED.name(),
                    0,
                    ttl
            );
        }

        long remaining = rule.getLimit() - count;

        log.debug("Rate limit check: clientId={}, endpoint={}, method={}, remaining={}",
                request.getClientId(), request.getEndpoint(), request.getHttpMethod(), remaining);

        return new RateLimitResponseDTO(
                true,
                DecisionStatus.ALLOWED.name(),
                remaining,
                ttl
        );
    }
}
