package io.github.valossa515.flow_guard_service.service;

import io.github.valossa515.flow_guard_service.config.DefaultRateLimitProperties;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.repository.RateLimitRuleRedisRepository;
import org.springframework.stereotype.Service;

@Service
public class RateLimitRuleResolver {

    private final RateLimitRuleRedisRepository redisRepository;
    private final DefaultRateLimitProperties defaultRule;

    public RateLimitRuleResolver(
            RateLimitRuleRedisRepository redisRepository,
            DefaultRateLimitProperties defaultRule
    ) {
        this.redisRepository = redisRepository;
        this.defaultRule = defaultRule;
    }

    public RateLimitRule resolve(RateLimitRequestDTO request) {

        return redisRepository
                .find(
                        request.getClientId(),
                        request.getEndpoint(),
                        request.getHttpMethod()
                )
                .orElseGet(this::defaultRule);
    }

    private RateLimitRule defaultRule() {
        RateLimitRule rule = new RateLimitRule();
        rule.setLimit(defaultRule.getLimit());
        rule.setWindowSeconds(defaultRule.getWindowSeconds());
        return rule;
    }
}