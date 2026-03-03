package io.github.valossa515.flow_guard_service.repository;

import io.github.valossa515.flow_guard_service.config.RedisKeyConstants;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitRuleRedisRepositoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private RateLimitRuleRedisRepository repository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        repository = new RateLimitRuleRedisRepository(redisTemplate);
    }

    @Test
    void shouldSaveRuleToRedis() {
        RateLimitRule rule = new RateLimitRule("client-1", "/api/test", "POST", 10, 60);

        repository.save(rule);

        String expectedKey = RedisKeyConstants.buildRuleKey("client-1", "/api/test", "POST");
        verify(hashOperations).put(expectedKey, "limit", "10");
        verify(hashOperations).put(expectedKey, "window", "60");
    }

    @Test
    void shouldFindExistingRule() {
        String key = RedisKeyConstants.buildRuleKey("client-1", "/api/test", "GET");
        Map<Object, Object> data = new HashMap<>();
        data.put("limit", "50");
        data.put("window", "120");

        when(hashOperations.entries(key)).thenReturn(data);

        Optional<RateLimitRule> result = repository.find("client-1", "/api/test", "GET");

        assertThat(result).isPresent();
        RateLimitRule rule = result.get();
        assertThat(rule.getClientId()).isEqualTo("client-1");
        assertThat(rule.getEndpoint()).isEqualTo("/api/test");
        assertThat(rule.getHttpMethod()).isEqualTo("GET");
        assertThat(rule.getLimit()).isEqualTo(50);
        assertThat(rule.getWindowSeconds()).isEqualTo(120);
    }

    @Test
    void shouldReturnEmptyWhenRuleNotFound() {
        String key = RedisKeyConstants.buildRuleKey("unknown", "/api/unknown", "GET");

        when(hashOperations.entries(key)).thenReturn(new HashMap<>());

        Optional<RateLimitRule> result = repository.find("unknown", "/api/unknown", "GET");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenDataIsIncomplete() {
        String key = RedisKeyConstants.buildRuleKey("client-1", "/api/test", "GET");
        Map<Object, Object> data = new HashMap<>();
        data.put("limit", "50");
        // missing "window"

        when(hashOperations.entries(key)).thenReturn(data);

        Optional<RateLimitRule> result = repository.find("client-1", "/api/test", "GET");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteRule() {
        String key = RedisKeyConstants.buildRuleKey("client-1", "/api/test", "POST");

        repository.delete("client-1", "/api/test", "POST");

        verify(redisTemplate).delete(key);
    }
}
