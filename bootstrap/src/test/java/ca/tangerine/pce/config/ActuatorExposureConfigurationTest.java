package ca.tangerine.pce.config;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ActuatorExposureConfigurationTest {

  private static final String APPLICATION_YAML = "application.yml";
  private static final String ACTUATOR_EXPOSURE_INCLUDE = "management.endpoints.web.exposure.include";
  private static final String HEALTH_STATUS_ORDER = "management.endpoint.health.status.order";
  private static final String CALCULATION_PERCENTILES = "management.metrics.distribution.percentiles.portfolio.calculation";
  private static final String HTTP_EXCHANGES_ENDPOINT = "httpexchanges";
  private static final String METRICS_ENDPOINT = "metrics";
  private static final String CALCULATION_STATISTICS_ENDPOINT = "calculationstats";
  private static final String CIRCUIT_BREAKERS_ENDPOINT = "circuitbreakers";
  private static final String RETRIES_ENDPOINT = "retries";
  private static final String CIRCUIT_BREAKER_EVENTS_ENDPOINT = "circuitbreakerevents";
  private static final String RETRY_EVENTS_ENDPOINT = "retryevents";
  private static final String CIRCUIT_OPEN_STATUS = "CIRCUIT_OPEN";
  private static final String CIRCUIT_HALF_OPEN_STATUS = "CIRCUIT_HALF_OPEN";

  /**
   * {@code httpexchanges} replays request and response bodies, and {@code metrics} enumerates every meter name and tag
   * value the process holds. Neither is fit to serve over the application port, no matter how useful it is while
   * debugging. The {@code *events} resilience endpoints replay a rolling buffer of upstream exception messages and fall
   * in the same category. The curated {@code calculationstats} read model and the bounded {@code circuitbreakers} and
   * {@code retries} state endpoints are the sanctioned way to read this over HTTP, so this pins them open as
   * deliberately as it pins the others shut.
   */
  @Test
  void shouldExposeOnlyTheCuratedEndpoints_whenDefaultActuatorExposureIsLoaded() throws IOException {
    Object configuredEndpoints = property(ACTUATOR_EXPOSURE_INCLUDE);

    assertThat(configuredEndpoints)
        .isInstanceOfSatisfying(String.class, endpoints -> assertThat(Arrays.stream(endpoints.split(","))
            .map(String::trim)
            .toList())
            .contains("health", "info", CALCULATION_STATISTICS_ENDPOINT, CIRCUIT_BREAKERS_ENDPOINT, RETRIES_ENDPOINT)
            .doesNotContain(HTTP_EXCHANGES_ENDPOINT, METRICS_ENDPOINT, CIRCUIT_BREAKER_EVENTS_ENDPOINT,
                RETRY_EVENTS_ENDPOINT));
  }

  /**
   * Resilience4j reports an open breaker as its own health status. A status left out of the order is treated as an
   * unknown code and sorts as the least severe there is, so omitting these two would silently hide every open breaker
   * behind an {@code UP} aggregate. The order must also sit under {@code management.endpoint.health}, because
   * {@code management.health.status.order} binds to nothing and is ignored without warning.
   */
  @Test
  void shouldRankTheBreakerStatusesBelowRealFailures_soAnOpenBreakerIsVisibleButNeverFailsAProbe() throws IOException {
    assertThat(property(HEALTH_STATUS_ORDER))
        .isInstanceOfSatisfying(String.class, order -> {
          var statuses = Arrays.stream(order.split(",")).map(String::trim).toList();

          assertThat(statuses).containsSubsequence("DOWN", "OUT_OF_SERVICE", CIRCUIT_OPEN_STATUS,
              CIRCUIT_HALF_OPEN_STATUS, "UP", "UNKNOWN");
        });
  }

  @Test
  void shouldRetainCalculationPercentiles_whenAzureMonitorConfigurationIsRemoved() throws IOException {
    assertThat(property(CALCULATION_PERCENTILES))
        .isInstanceOfSatisfying(String.class, percentiles -> assertThat(Arrays.stream(percentiles.split(","))
            .map(String::trim)
            .toList())
            .containsExactly("0.5", "0.95", "0.99"));
  }

  private static Object property(String name) throws IOException {
    return new YamlPropertySourceLoader()
        .load(APPLICATION_YAML, new ClassPathResource(APPLICATION_YAML))
        .getFirst()
        .getProperty(name);
  }
}
