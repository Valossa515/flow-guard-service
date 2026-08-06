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
    private RateLimitRuleRedisRepository repository;

    private RateLimitRuleResolver resolver;

    @BeforeEach
    void setUp() {
        DefaultRateLimitProperties defaults = new DefaultRateLimitProperties();
        defaults.setLimit(100);
        defaults.setWindowSeconds(60);
        resolver = new RateLimitRuleResolver(repository, defaults);
    }

    @Test
    void shouldReturnCustomRuleWhenItExists() {
        RateLimitRule custom = new RateLimitRule("client-1", "/api/orders", "GET", 10, 30);
        when(repository.find("client-1", "/api/orders", "GET")).thenReturn(Optional.of(custom));

        RateLimitRule resolved = resolver.resolve(request("client-1", "/api/orders", "GET"));

        assertThat(resolved.getLimit()).isEqualTo(10);
        assertThat(resolved.getWindowSeconds()).isEqualTo(30);
    }

    @Test
    void shouldFallBackToDefaultRuleWhenNoCustomRuleExists() {
        when(repository.find("client-2", "/api/orders", "POST")).thenReturn(Optional.empty());

        RateLimitRule resolved = resolver.resolve(request("client-2", "/api/orders", "POST"));

        assertThat(resolved.getLimit()).isEqualTo(100);
        assertThat(resolved.getWindowSeconds()).isEqualTo(60);
    }

    private RateLimitRequestDTO request(String clientId, String endpoint, String method) {
        RateLimitRequestDTO request = new RateLimitRequestDTO();
        request.setClientId(clientId);
        request.setEndpoint(endpoint);
        request.setHttpMethod(method);
        return request;
    }
}
