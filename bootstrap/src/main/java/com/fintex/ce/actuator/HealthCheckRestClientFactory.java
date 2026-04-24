package com.fintex.ce.actuator;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Builds a {@link RestClient} tuned for liveness/readiness checks: a short connect and read timeout so a hung
 * downstream fails the probe quickly rather than blocking the probe thread until the Kubernetes probe timeout hits.
 */
final class HealthCheckRestClientFactory {

  static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(3);

  private HealthCheckRestClientFactory() {
  }

  static RestClient create(RestClient.Builder builder, String baseUrl) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) HEALTH_CHECK_TIMEOUT.toMillis());
    factory.setReadTimeout((int) HEALTH_CHECK_TIMEOUT.toMillis());
    return builder.baseUrl(baseUrl).requestFactory(factory).build();
  }
}