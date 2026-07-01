package com.fintex.ce.config;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import org.junit.jupiter.api.Test;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;

import static org.assertj.core.api.Assertions.assertThat;

class AzureMonitorOpenTelemetryConfigurationTest {

  private static final String CONNECTION_STRING = "InstrumentationKey=00000000-0000-0000-0000-000000000000;"
      + "IngestionEndpoint=https://example.com/";

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(AzureMonitorOpenTelemetryConfiguration.class);

  @Test
  void shouldCreateSpringManagedOpenTelemetry_whenAzureMonitorConnectionStringIsConfigured() {
    String previousServiceName = System.getProperty(AzureMonitorProperties.OTEL_SERVICE_NAME);
    try {
      System.clearProperty(AzureMonitorProperties.OTEL_SERVICE_NAME);

      contextRunner
          .withPropertyValues(
              "spring.application.name=calculation-engine-test",
              "observability.azure-monitor.enabled=true",
              "observability.azure-monitor.connection-string=" + CONNECTION_STRING,
              "observability.azure-monitor.live-metrics.enabled=false")
          .run(context -> {
            assertThat(context).hasSingleBean(OpenTelemetrySdk.class);
            assertThat(context).hasSingleBean(OpenTelemetry.class);
            assertThat(System.getProperty(AzureMonitorProperties.OTEL_SERVICE_NAME)).isNull();
          });
    } finally {
      restoreOtelServiceName(previousServiceName);
    }
  }

  @Test
  void shouldNotCreateOpenTelemetryBean_whenAzureMonitorIsDisabled() {
    contextRunner
        .withPropertyValues(
            "observability.azure-monitor.enabled=false",
            "observability.azure-monitor.connection-string=" + CONNECTION_STRING)
        .run(context -> assertThat(context).doesNotHaveBean(OpenTelemetrySdk.class));
  }

  @Test
  void shouldResolveOtelPropagators_whenConfigured() {
    contextRunner
        .withPropertyValues("otel.propagators=tracecontext,baggage,b3")
        .run(context -> assertThat(AzureMonitorProperties.propagators(context.getEnvironment()))
            .isEqualTo("tracecontext,baggage,b3"));
  }

  private static void restoreOtelServiceName(String previousServiceName) {
    if (previousServiceName == null) {
      System.clearProperty(AzureMonitorProperties.OTEL_SERVICE_NAME);
      return;
    }
    System.setProperty(AzureMonitorProperties.OTEL_SERVICE_NAME, previousServiceName);
  }
}
