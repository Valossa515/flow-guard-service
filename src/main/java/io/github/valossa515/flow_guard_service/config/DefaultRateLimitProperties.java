package io.github.valossa515.flow_guard_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate-limit.default-rule")
@Data
public class DefaultRateLimitProperties {
    private long limit;
    private long windowSeconds;
}
