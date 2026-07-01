package com.fintex.ce.config;

import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
class AzureMonitorProperties {

  static final String ENABLED_PROPERTY = "observability.azure-monitor.enabled";
  static final String CONNECTION_STRING_PROPERTY = "observability.azure-monitor.connection-string";
  static final String LIVE_METRICS_ENABLED_PROPERTY = "observability.azure-monitor.live-metrics.enabled";
  static final String APPLICATION_INSIGHTS_CONNECTION_STRING = "APPLICATIONINSIGHTS_CONNECTION_STRING";
  static final String APPLICATION_INSIGHTS_CONNECTION_STRING_PROPERTY = "applicationinsights.connection.string";
  static final String APPLICATION_INSIGHTS_LIVE_METRICS_ENABLED = "applicationinsights.live.metrics.enabled";
  static final String OTEL_SERVICE_NAME = "otel.service.name";
  static final String OTEL_PROPAGATORS = "otel.propagators";
  static final String SPRING_APPLICATION_NAME = "spring.application.name";
  static final String DEFAULT_SERVICE_NAME = "calculation-engine";
  static final String DEFAULT_OTEL_PROPAGATORS = "tracecontext,baggage,b3";

  static boolean azureMonitorEnabled(Environment environment) {
    return environment.getProperty(ENABLED_PROPERTY, Boolean.class, true);
  }

  static boolean liveMetricsEnabled(Environment environment) {
    return environment.getProperty(LIVE_METRICS_ENABLED_PROPERTY, Boolean.class, true);
  }

  static String azureMonitorConnectionString(Environment environment) {
    return firstNonBlank(
        environment.getProperty(CONNECTION_STRING_PROPERTY),
        environment.getProperty(APPLICATION_INSIGHTS_CONNECTION_STRING),
        environment.getProperty(APPLICATION_INSIGHTS_CONNECTION_STRING_PROPERTY));
  }

  static String serviceName(Environment environment) {
    return firstNonBlank(
        environment.getProperty(OTEL_SERVICE_NAME),
        environment.getProperty(SPRING_APPLICATION_NAME),
        DEFAULT_SERVICE_NAME);
  }

  static String propagators(Environment environment) {
    return firstNonBlank(
        environment.getProperty(OTEL_PROPAGATORS),
        DEFAULT_OTEL_PROPAGATORS);
  }

  static String firstNonBlank(String... values) {
    return Stream.of(values)
        .filter(Objects::nonNull)
        .filter(value -> !value.isBlank())
        .findFirst()
        .orElse(null);
  }
}
