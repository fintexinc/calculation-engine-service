package com.fintex.ce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenAPIConfig {

    @Bean
    public GroupedOpenApi serviceApi() {
        return GroupedOpenApi.builder()
                .group("portfolio-calculation-service")
                .pathsToMatch("/portfolio/**", "/tasks/**")
                .build();
    }

    @Bean
    public OpenAPI pcsOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Portfolio Calculation Service"));
    }
}
