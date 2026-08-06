package com.fintex.ce.adapter.webclient.sm.integration;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClientConfig;
import com.fintex.ce.adapter.webclient.sm.fetcher.SecurityAttributeFetcherConfig;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;

/**
 * Minimal Spring context for SM REST integration tests in this module. The full CE application lives in {@code
 * bootstrap}, which already depends on {@code web-client-adapter}, so tests here cannot load that main class without a
 * Maven cycle. This class wires only the Security Master WebClient, the generic attributes fetcher with its binding
 * registry, and the mappers.
 *
 * <p>
 * The registries are declared here because this module has no actuator on its classpath to auto-configure them, while
 * production always does. They are real registries, not stand-ins: the observability component requires them rather
 * than quietly substituting a private one, so a deployment missing them fails at startup instead of losing its metrics
 * in silence.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import({SecurityMasterWebClientConfig.class, SecurityAttributeFetcherConfig.class})
@ComponentScan(basePackages = {"com.fintex.ce.adapter.webclient.sm.client",
    "com.fintex.ce.adapter.webclient.observability",
    "com.fintex.ce.adapter.webclient.sm.mapper"})
public class SecurityMasterWebClientIntegrationTestConfiguration {

  @Bean
  public ObservationRegistry observationRegistry() {
    return ObservationRegistry.create();
  }

  @Bean
  public MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }
}
