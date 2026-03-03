package io.github.valossa515.flow_guard_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI rateLimitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flow Guard Service API")
                        .description("Microservice for API rate limiting. Provides endpoints to check rate limits "
                                + "per client/endpoint/method and manage custom rate limit rules.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Felipe Oliveira")
                                .url("https://github.com/Valossa515/flow-guard-service")))
                .servers(List.of(
                        new Server().url("/").description("Current server")
                ));
    }
}
