package io.github.valossa515.flow_guard_service.service.impl;

import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.service.RateLimitRuleResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitRedisServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RateLimitRuleResolver ruleResolver;

    private RateLimitRedisServiceImpl service;

    private final RateLimitRule rule = new RateLimitRule("client-1", "/api/orders", "GET", 5, 60);

    @BeforeEach
    void setUp() {
        service = new RateLimitRedisServiceImpl(redisTemplate, ruleResolver);
        when(ruleResolver.resolve(any())).thenReturn(rule);
    }

    @Test
    void shouldAllowRequestWithinLimit() {
        mockScriptResult(1L, 60L);

        RateLimitResponseDTO response = service.checkRateLimit(request());

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.getDecision()).isEqualTo(DecisionStatus.ALLOWED.name());
        assertThat(response.getRemainingRequests()).isEqualTo(4);
        assertThat(response.getResetInSeconds()).isEqualTo(60);
    }

    @Test
    void shouldAllowLastRequestOfTheWindowWithZeroRemaining() {
        mockScriptResult(5L, 42L);

        RateLimitResponseDTO response = service.checkRateLimit(request());

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.getRemainingRequests()).isZero();
        assertThat(response.getResetInSeconds()).isEqualTo(42);
    }

    @Test
    void shouldBlockRequestAboveLimit() {
        mockScriptResult(6L, 30L);

        RateLimitResponseDTO response = service.checkRateLimit(request());

        assertThat(response.isAllowed()).isFalse();
        assertThat(response.getDecision()).isEqualTo(DecisionStatus.RATE_LIMIT_EXCEEDED.name());
        assertThat(response.getRemainingRequests()).isZero();
        assertThat(response.getResetInSeconds()).isEqualTo(30);
    }

    @Test
    void shouldFallBackToWindowSecondsWhenTtlIsMissing() {
        mockScriptResult(1L, -1L);

        RateLimitResponseDTO response = service.checkRateLimit(request());

        assertThat(response.getResetInSeconds()).isEqualTo(rule.getWindowSeconds());
    }

    @SuppressWarnings("unchecked")
    private void mockScriptResult(long count, long ttl) {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(List.of(count, ttl));
    }

    private RateLimitRequestDTO request() {
        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setClientId("client-1");
        request.setEndpoint("/api/orders");
        request.setHttpMethod("GET");
        return request;
    }
}
