package io.github.valossa515.flow_guard_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI rateLimitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rate Limit Service API")
                        .description("API para verificação de rate limit por cliente")
                        .version("v1"));
    }
}