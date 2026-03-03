package io.github.valossa515.flow_guard_service.service;

import io.github.valossa515.flow_guard_service.dto.CreateRateLimitRuleRequest;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.repository.RateLimitRuleRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitAdminServiceTest {

    @Mock
    private RateLimitRuleRedisRepository repository;

    private RateLimitAdminService service;

    @BeforeEach
    void setUp() {
        service = new RateLimitAdminService(repository);
    }

    @Test
    void shouldCreateRuleFromRequest() {
        CreateRateLimitRuleRequest request = new CreateRateLimitRuleRequest();
        request.setClientId("client-1");
        request.setEndpoint("/api/test");
        request.setHttpMethod("POST");
        request.setLimit(20);
        request.setWindowSeconds(30);

        service.create(request);

        ArgumentCaptor<RateLimitRule> captor = ArgumentCaptor.forClass(RateLimitRule.class);
        verify(repository).save(captor.capture());

        RateLimitRule saved = captor.getValue();
        assertThat(saved.getClientId()).isEqualTo("client-1");
        assertThat(saved.getEndpoint()).isEqualTo("/api/test");
        assertThat(saved.getHttpMethod()).isEqualTo("POST");
        assertThat(saved.getLimit()).isEqualTo(20);
        assertThat(saved.getWindowSeconds()).isEqualTo(30);
    }

    @Test
    void shouldDelegateGetToRepository() {
        RateLimitRule rule = new RateLimitRule("client-1", "/api/test", "GET", 10, 60);
        when(repository.find("client-1", "/api/test", "GET"))
                .thenReturn(Optional.of(rule));

        Optional<RateLimitRule> result = service.get("client-1", "/api/test", "GET");

        assertThat(result).isPresent();
        assertThat(result.get().getLimit()).isEqualTo(10);
    }

    @Test
    void shouldReturnEmptyWhenRuleNotFound() {
        when(repository.find("unknown", "/api/unknown", "GET"))
                .thenReturn(Optional.empty());

        Optional<RateLimitRule> result = service.get("unknown", "/api/unknown", "GET");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDelegateDeleteToRepository() {
        service.delete("client-1", "/api/test", "POST");

        verify(repository).delete("client-1", "/api/test", "POST");
    }
}
