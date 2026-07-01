package com.fintex.ce.config;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ActuatorExposureConfigurationTest {

  private static final String APPLICATION_YAML = "application.yml";
  private static final String ACTUATOR_EXPOSURE_INCLUDE = "management.endpoints.web.exposure.include";
  private static final String HTTP_EXCHANGES_ENDPOINT = "httpexchanges";
  private static final String METRICS_ENDPOINT = "metrics";

  @Test
  void shouldNotExposeHttpExchanges_whenDefaultActuatorExposureIsLoaded() throws IOException {
    Object configuredEndpoints = new YamlPropertySourceLoader()
        .load(APPLICATION_YAML, new ClassPathResource(APPLICATION_YAML))
        .getFirst()
        .getProperty(ACTUATOR_EXPOSURE_INCLUDE);

    assertThat(configuredEndpoints)
        .isInstanceOfSatisfying(String.class, endpoints -> assertThat(Arrays.stream(endpoints.split(","))
            .map(String::trim)
            .toList())
            .contains("health", "info")
            .doesNotContain(HTTP_EXCHANGES_ENDPOINT, METRICS_ENDPOINT));
  }
}
