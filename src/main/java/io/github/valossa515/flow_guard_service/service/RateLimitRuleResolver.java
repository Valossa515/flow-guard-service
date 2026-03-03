package io.github.valossa515.flow_guard_service.service;

import io.github.valossa515.flow_guard_service.config.DefaultRateLimitProperties;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.repository.RateLimitRuleRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RateLimitRuleResolver {

    private static final Logger log = LoggerFactory.getLogger(RateLimitRuleResolver.class);

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
                .orElseGet(() -> {
                    log.debug("No custom rule found for clientId={}, endpoint={}, method={}. Using default rule.",
                            request.getClientId(), request.getEndpoint(), request.getHttpMethod());
                    return defaultRule();
                });
    }

    private RateLimitRule defaultRule() {
        RateLimitRule rule = new RateLimitRule();
        rule.setLimit(defaultRule.getLimit());
        rule.setWindowSeconds(defaultRule.getWindowSeconds());
        return rule;
    }
}
