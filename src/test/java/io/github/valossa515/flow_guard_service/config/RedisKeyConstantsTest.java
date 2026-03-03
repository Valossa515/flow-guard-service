package io.github.valossa515.flow_guard_service.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedisKeyConstantsTest {

    @Test
    void shouldBuildCounterKey() {
        String key = RedisKeyConstants.buildCounterKey("client-1", "/api/test", "POST");

        assertThat(key).isEqualTo("rate-limit:client-1:/api/test:POST");
    }

    @Test
    void shouldBuildRuleKey() {
        String key = RedisKeyConstants.buildRuleKey("client-1", "/api/test", "GET");

        assertThat(key).isEqualTo("rate-limit:rule:client-1:/api/test:GET");
    }
}
