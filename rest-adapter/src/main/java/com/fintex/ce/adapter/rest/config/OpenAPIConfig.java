package com.fintex.ce.adapter.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

  @Bean
  public OpenAPI pcsOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Portfolio Calculation Engine API")
            .description("Portfolio analytics and risk measurement engine. "
                + "Calculates returns, risk metrics, risk-adjusted ratios, "
                + "portfolio composition, fees, and forecasts.")
            .version("1.0.0")
                .contact(new Contact()
                        .name("Digital Wealth Team")
                        .email("kparamsothy@tangerine.ca"))
                .license(new License().name("Portfolio Calculation Engine")));
  }
}
