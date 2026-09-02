package ca.tangerine.pce.actuator;

import ca.tangerine.pce.webclient.mic.client.MarketInvestmentCatalogueRestProperties;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Reports the availability of the downstream Market Investment Catalogue Service by calling its actuator health
 * endpoint. Gates the {@code readiness} probe because every calculation metric depends on MIC data.
 */
@Slf4j
@Component
public class MicHealthIndicator implements HealthIndicator {

  private static final String STATUS_DETAIL = "status";

  private final RestClient restClient;
  private final String healthCheckPath;

  public MicHealthIndicator(MarketInvestmentCatalogueRestProperties properties, RestClient.Builder builder) {
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
      log.warn("MIC health check failed: {}", e.getMessage());
      return Health.down().withDetail("exception", e.getMessage()).build();
    }
  }
}