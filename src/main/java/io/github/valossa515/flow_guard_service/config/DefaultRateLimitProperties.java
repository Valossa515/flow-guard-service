package io.github.valossa515.flow_guard_service.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "rate-limit.default-rule")
@Validated
@Data
public class DefaultRateLimitProperties {

    @Min(value = 1, message = "Default limit must be at least 1")
    private long limit;

    @Min(value = 1, message = "Default windowSeconds must be at least 1")
    private long windowSeconds;
}
