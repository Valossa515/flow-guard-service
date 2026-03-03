package io.github.valossa515.flow_guard_service.service.impl;

import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.service.RateLimitRuleResolver;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitRedisServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RateLimitRuleResolver ruleResolver;

    private RateLimitRedisServiceImpl service;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new RateLimitRedisServiceImpl(redisTemplate, ruleResolver, meterRegistry);
    }

    private RateLimitRequestDTO buildRequest() {
        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("GET");
        return request;
    }

    private RateLimitRule buildRule(long limit, long windowSeconds) {
        return new RateLimitRule("client-1", "/api/test", "GET", limit, windowSeconds);
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() {
        RateLimitRequestDTO request = buildRequest();
        RateLimitRule rule = buildRule(10, 60);

        when(ruleResolver.resolve(request)).thenReturn(rule);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(List.of(3L, 55L));

        RateLimitResponseDTO response = service.checkRateLimit(request);

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.getDecision()).isEqualTo(DecisionStatus.ALLOWED.name());
        assertThat(response.getRemainingRequests()).isEqualTo(7);
        assertThat(response.getResetInSeconds()).isEqualTo(55);
    }

    @Test
    void shouldBlockRequestWhenOverLimit() {
        RateLimitRequestDTO request = buildRequest();
        RateLimitRule rule = buildRule(5, 60);

        when(ruleResolver.resolve(request)).thenReturn(rule);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(List.of(6L, 45L));

        RateLimitResponseDTO response = service.checkRateLimit(request);

        assertThat(response.isAllowed()).isFalse();
        assertThat(response.getDecision()).isEqualTo(DecisionStatus.RATE_LIMIT_EXCEEDED.name());
        assertThat(response.getRemainingRequests()).isEqualTo(0);
        assertThat(response.getResetInSeconds()).isEqualTo(45);
    }

    @Test
    void shouldAllowFirstRequest() {
        RateLimitRequestDTO request = buildRequest();
        RateLimitRule rule = buildRule(100, 60);

        when(ruleResolver.resolve(request)).thenReturn(rule);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(List.of(1L, 60L));

        RateLimitResponseDTO response = service.checkRateLimit(request);

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.getRemainingRequests()).isEqualTo(99);
    }

    @Test
    void shouldUseWindowSecondsWhenTtlIsNegative() {
        RateLimitRequestDTO request = buildRequest();
        RateLimitRule rule = buildRule(10, 60);

        when(ruleResolver.resolve(request)).thenReturn(rule);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(List.of(2L, -1L));

        RateLimitResponseDTO response = service.checkRateLimit(request);

        assertThat(response.getResetInSeconds()).isEqualTo(60);
    }

    @Test
    void shouldReturnZeroRemainingWhenAtExactLimit() {
        RateLimitRequestDTO request = buildRequest();
        RateLimitRule rule = buildRule(5, 60);

        when(ruleResolver.resolve(request)).thenReturn(rule);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(List.of(5L, 30L));

        RateLimitResponseDTO response = service.checkRateLimit(request);

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.getRemainingRequests()).isEqualTo(0);
    }

    @Test
    void shouldIncrementChecksMetric() {
        RateLimitRequestDTO request = buildRequest();
        RateLimitRule rule = buildRule(10, 60);

        when(ruleResolver.resolve(request)).thenReturn(rule);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(List.of(1L, 60L));

        service.checkRateLimit(request);
        service.checkRateLimit(request);

        double count = meterRegistry.counter("rate_limit.checks").count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void shouldIncrementBlockedMetricWhenExceeded() {
        RateLimitRequestDTO request = buildRequest();
        RateLimitRule rule = buildRule(1, 60);

        when(ruleResolver.resolve(request)).thenReturn(rule);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(List.of(2L, 55L));

        service.checkRateLimit(request);

        double blocked = meterRegistry.counter("rate_limit.blocked").count();
        assertThat(blocked).isEqualTo(1.0);
    }
}
