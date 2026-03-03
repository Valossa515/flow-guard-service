package io.github.valossa515.flow_guard_service.service;

import io.github.valossa515.flow_guard_service.config.DefaultRateLimitProperties;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.repository.RateLimitRuleRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitRuleResolverTest {

    @Mock
    private RateLimitRuleRedisRepository redisRepository;

    @Mock
    private DefaultRateLimitProperties defaultProperties;

    private RateLimitRuleResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new RateLimitRuleResolver(redisRepository, defaultProperties);
    }

    @Test
    void shouldReturnCustomRuleWhenExists() {
        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("POST");

        RateLimitRule customRule = new RateLimitRule("client-1", "/api/test", "POST", 50, 120);

        when(redisRepository.find("client-1", "/api/test", "POST"))
                .thenReturn(Optional.of(customRule));

        RateLimitRule result = resolver.resolve(request);

        assertThat(result.getLimit()).isEqualTo(50);
        assertThat(result.getWindowSeconds()).isEqualTo(120);
        assertThat(result.getClientId()).isEqualTo("client-1");
    }

    @Test
    void shouldReturnDefaultRuleWhenNoCustomRuleExists() {
        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setClientId("unknown-client");
        request.setEndpoint("/api/unknown");
        request.setHttpMethod("GET");

        when(redisRepository.find("unknown-client", "/api/unknown", "GET"))
                .thenReturn(Optional.empty());
        when(defaultProperties.getLimit()).thenReturn(100L);
        when(defaultProperties.getWindowSeconds()).thenReturn(60L);

        RateLimitRule result = resolver.resolve(request);

        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getWindowSeconds()).isEqualTo(60);
    }
}
