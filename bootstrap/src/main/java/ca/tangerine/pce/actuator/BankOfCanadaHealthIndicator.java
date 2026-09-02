package ca.tangerine.pce.actuator;

import ca.tangerine.pce.webclient.boc.client.BankOfCanadaProperties;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Reports the availability of the Bank of Canada Valet API. Informational only — BoC outages degrade FX conversion for
 * multi-currency portfolios but never block calculations, so this indicator does not gate {@code readiness}.
 */
@Slf4j
@Component
public class BankOfCanadaHealthIndicator implements HealthIndicator {

  private static final String STATUS_DETAIL = "status";

  private final RestClient restClient;
  private final String healthCheckPath;

  public BankOfCanadaHealthIndicator(BankOfCanadaProperties properties, RestClient.Builder builder) {
    this.restClient = HealthCheckRestClientFactory.create(builder, properties.getBaseUrl());
    this.healthCheckPath = properties.getHealthCheckPath();
  }

  @Override
  public Health health() {
    try {
      ResponseEntity<Void> response = restClient.get()
          .uri(healthCheckPath)
          .retrieve()
          .onStatus(code -> !code.is2xxSuccessful(), (request, resp) -> {
          })
          .toBodilessEntity();
      int status = response.getStatusCode().value();
      return response.getStatusCode().is2xxSuccessful()
          ? Health.up().withDetail(STATUS_DETAIL, status).build()
          : Health.down().withDetail(STATUS_DETAIL, status).build();
    } catch (Exception e) {
      log.warn("Bank of Canada health check failed: {}", e.getMessage());
      return Health.down().withDetail("exception", e.getMessage()).build();
    }
  }
}