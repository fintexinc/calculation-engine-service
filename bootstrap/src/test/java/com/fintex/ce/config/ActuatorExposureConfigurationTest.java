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
  private static final String CALCULATION_STATISTICS_ENDPOINT = "calculationstats";

  /**
   * {@code httpexchanges} replays request and response bodies, and {@code metrics} enumerates every meter name and tag
   * value the process holds. Neither is fit to serve over the application port, no matter how useful it is while
   * debugging. The curated {@code calculationstats} read model is the sanctioned way to read calculation meters over
   * HTTP, so this pins it open as deliberately as it pins the other two shut.
   */
  @Test
  void shouldExposeOnlyTheCuratedEndpoints_whenDefaultActuatorExposureIsLoaded() throws IOException {
    Object configuredEndpoints = new YamlPropertySourceLoader()
        .load(APPLICATION_YAML, new ClassPathResource(APPLICATION_YAML))
        .getFirst()
        .getProperty(ACTUATOR_EXPOSURE_INCLUDE);

    assertThat(configuredEndpoints)
        .isInstanceOfSatisfying(String.class, endpoints -> assertThat(Arrays.stream(endpoints.split(","))
            .map(String::trim)
            .toList())
            .contains("health", "info", CALCULATION_STATISTICS_ENDPOINT)
            .doesNotContain(HTTP_EXCHANGES_ENDPOINT, METRICS_ENDPOINT));
  }
}
