package com.fintex.ce.adapter.webclient.resilience;

import com.fintex.wm.commons.domain.ExternalWebService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

/**
 * Resolves one {@link ExternalCallResilience} per {@link ExternalWebService} from the Resilience4j registries, so the
 * thresholds live in {@code resilience4j.circuitbreaker}, {@code resilience4j.retry} and
 * {@code resilience4j.timelimiter} configuration and each instance is registered — and therefore metered and exposed
 * over the actuator — under the service's own name. A service with no instance of its own falls back to the registries'
 * default configuration rather than to no protection at all.
 */
@Configuration
public class ExternalCallResilienceConfig {

  @Bean
  public ExternalCallResilience marketInvestmentCatalogueCallResilience(CircuitBreakerRegistry circuitBreakers,
      RetryRegistry retries, TimeLimiterRegistry timeLimiters) {
    return ExternalCallResilience.of(ExternalWebService.MARKET_INVESTMENT_CATALOGUE, circuitBreakers, retries,
        timeLimiters);
  }

  @Bean
  public ExternalCallResilience bankOfCanadaCallResilience(CircuitBreakerRegistry circuitBreakers,
      RetryRegistry retries, TimeLimiterRegistry timeLimiters) {
    return ExternalCallResilience.of(ExternalWebService.BANK_OF_CANADA, circuitBreakers, retries, timeLimiters);
  }
}
