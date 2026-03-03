package io.github.valossa515.flow_guard_service.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate redisTemplate;

    public RedisHealthIndicator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection().ping();
            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("redis", "connected")
                        .build();
            }
            return Health.down()
                    .withDetail("redis", "unexpected ping response: " + pong)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("redis", "connection failed")
                    .withException(e)
                    .build();
        }
    }
}
