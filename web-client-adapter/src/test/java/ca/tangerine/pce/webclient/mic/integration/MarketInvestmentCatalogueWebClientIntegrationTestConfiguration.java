package ca.tangerine.pce.webclient.mic.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import ca.tangerine.pce.port.observability.ExternalCallObservability;
import ca.tangerine.pce.webclient.mic.client.MarketInvestmentCatalogueWebClientConfig;
import ca.tangerine.pce.webclient.mic.fetcher.SecurityAttributeFetcherConfig;
import ca.tangerine.pce.webclient.resilience.ExternalCallResilienceConfig;

/**
 * Minimal Spring context for MIC REST integration tests in this module. The full CE application lives in {@code
 * bootstrap}, which already depends on {@code web-client-adapter}, so tests here cannot load that main class without a
 * Maven cycle. This class wires only the Market Investment Catalogue WebClient, the generic attributes fetcher with its
 * binding registry, and the mappers.
 *
 * <p>
 * The subject here is the client's HTTP behaviour, and the implementation that turns a call into metrics and spans
 * lives in a module this one does not depend on, so the observability port is wired to
 * {@link ExternalCallObservability#NO_OP}. That is a decision stated here rather than a fallback the client makes for
 * itself: the client requires the port, so a deployment missing it fails at startup instead of losing its telemetry
 * quietly. What the real implementation publishes is covered where it lives, and the two together are exercised by the
 * e2e suite.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import({MarketInvestmentCatalogueWebClientConfig.class, SecurityAttributeFetcherConfig.class,
    ExternalCallResilienceConfig.class})
@ComponentScan(basePackages = {
    "ca.tangerine.pce.webclient.mic",
    "ca.tangerine.pce.webclient.observability"})
public class MarketInvestmentCatalogueWebClientIntegrationTestConfiguration {

  @Bean
  public ExternalCallObservability externalCallObservability() {
    return ExternalCallObservability.NO_OP;
  }
}
