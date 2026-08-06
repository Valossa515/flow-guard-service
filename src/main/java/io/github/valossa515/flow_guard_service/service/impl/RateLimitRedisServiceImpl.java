package io.github.valossa515.flow_guard_service.service.impl;

import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.service.RateLimitRuleResolver;
import io.github.valossa515.flow_guard_service.service.RateLimitService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimitRedisServiceImpl implements RateLimitService {

    /**
     * INCR + EXPIRE precisam acontecer na mesma operação: se fossem duas
     * chamadas e o processo caísse entre elas, a chave ficaria sem TTL e o
     * cliente bloqueado para sempre. O script devolve [contagem, ttl].
     */
    private static final RedisScript<List> RATE_LIMIT_SCRIPT = RedisScript.of("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            local ttl = redis.call('TTL', KEYS[1])
            return {count, ttl}
            """, List.class);

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

        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(rule.getWindowSeconds())
        );

        long count = result.get(0);
        long ttl = result.get(1);
        long resetInSeconds = ttl > 0 ? ttl : rule.getWindowSeconds();

        if (count > rule.getLimit()) {
            return new RateLimitResponseDTO(
                    false,
                    DecisionStatus.RATE_LIMIT_EXCEEDED.name(),
                    0,
                    resetInSeconds
            );
        }

        return new RateLimitResponseDTO(
                true,
                DecisionStatus.ALLOWED.name(),
                rule.getLimit() - count,
                resetInSeconds
        );
    }

    private String buildKey(RateLimitRequestDTO request) {
        return "rate-limit:"
                + request.getClientId()
                + ":" + request.getEndpoint()
                + ":" + request.getHttpMethod();
    }
}
