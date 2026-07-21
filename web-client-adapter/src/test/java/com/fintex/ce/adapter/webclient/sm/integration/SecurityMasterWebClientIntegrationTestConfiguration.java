package com.fintex.ce.adapter.webclient.sm.integration;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClientConfig;
import com.fintex.ce.adapter.webclient.sm.fetcher.SecurityAttributeFetcherConfig;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Minimal Spring context for SM REST integration tests in this module. The full CE application lives in {@code
 * bootstrap}, which already depends on {@code web-client-adapter}, so tests here cannot load that main class without a
 * Maven cycle. This class wires only the Security Master WebClient, the generic attributes fetcher with its binding
 * registry, and the mappers.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import({SecurityMasterWebClientConfig.class, SecurityAttributeFetcherConfig.class})
@ComponentScan(basePackages = {"com.fintex.ce.adapter.webclient.sm.client",
    "com.fintex.ce.adapter.webclient.observability",
    "com.fintex.ce.adapter.webclient.sm.mapper"})
public class SecurityMasterWebClientIntegrationTestConfiguration {
}
