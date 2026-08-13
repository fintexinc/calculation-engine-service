package com.fintex.ce.adapter.webclient.sm.integration;

import com.fintex.ce.adapter.webclient.resilience.ExternalCallResilienceConfig;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClientConfig;
import com.fintex.ce.adapter.webclient.sm.fetcher.SecurityAttributeFetcherConfig;
import com.fintex.ce.port.observability.ExternalCallObservability;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Minimal Spring context for SM REST integration tests in this module. The full CE application lives in {@code
 * bootstrap}, which already depends on {@code web-client-adapter}, so tests here cannot load that main class without a
 * Maven cycle. This class wires only the Security Master WebClient, the generic attributes fetcher with its binding
 * registry, and the mappers.
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
@Import({SecurityMasterWebClientConfig.class, SecurityAttributeFetcherConfig.class,
    ExternalCallResilienceConfig.class})
@ComponentScan(basePackages = {"com.fintex.ce.adapter.webclient.sm.client",
    "com.fintex.ce.adapter.webclient.sm.fetcher",
    "com.fintex.ce.adapter.webclient.observability",
    "com.fintex.ce.adapter.webclient.sm.mapper"})
public class SecurityMasterWebClientIntegrationTestConfiguration {

  @Bean
  public ExternalCallObservability externalCallObservability() {
    return ExternalCallObservability.NO_OP;
  }
}
