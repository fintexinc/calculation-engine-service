package ca.tangerine.pce.config;

import ca.tangerine.pce.webclient.resilience.ExternalCallResilience;
import ca.tangerine.pce.webclient.resilience.ExternalCallResilienceConfig;
import ca.tangerine.wm.commons.domain.ExternalWebService;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterAutoConfiguration;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the retry and circuit breaker configuration in {@code application.yml} against the instance names the clients
 * resolve, because every way of getting it wrong is silent. An instance renamed on one side of the wiring still yields
 * a working {@link ExternalCallResilience} — it just quietly falls back to the {@code default} configuration and loses
 * the per-provider tuning — and a predicate left off leaves the breaker counting rejected requests as provider outages.
 * Nothing at startup reports either.
 *
 * <p>
 * The services are named explicitly rather than taken from {@link ExternalWebService} as a whole, because that enum is
 * shared across services and names providers this one never calls; only the ones its clients resolve must be
 * configured. The predicate is asserted through the configuration rather than by identity, since Resilience4j wraps it
 * together with its own exception lists; what matters is the verdict the breaker and the retry actually reach.
 */
class ExternalCallResilienceConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withInitializer(new ConfigDataApplicationContextInitializer())
      .withConfiguration(AutoConfigurations.of(CircuitBreakerAutoConfiguration.class, RetryAutoConfiguration.class,
          TimeLimiterAutoConfiguration.class))
      .withUserConfiguration(ExternalCallResilienceConfig.class);

  @ParameterizedTest
  @EnumSource(value = ExternalWebService.class, names = {"MARKET_INVESTMENT_CATALOGUE", "BANK_OF_CANADA"})
  void shouldRegisterARetryABreakerAndADeadline_forEveryServiceTheClientsResolve(ExternalWebService service) {
    contextRunner.run(context -> {
      assertThat(circuitBreakerConfig(context, service)).isNotNull();
      assertThat(retryConfig(context, service)).isNotNull();
      assertThat(timeLimiterConfig(context, service)).isNotNull();
    });
  }

  /**
   * The whole-call deadline must leave room for at least one full attempt at the per-attempt HTTP timeout, or a call
   * that is merely slow could never succeed at all; and it must exist as an explicit figure well above Resilience4j's
   * 1-second fallback, which no real call here can meet.
   */
  @ParameterizedTest
  @CsvSource({
      "MARKET_INVESTMENT_CATALOGUE, external-services.market-investment-catalogue.rest.timeout",
      "BANK_OF_CANADA, external-services.bank-of-canada.timeout"})
  void shouldBudgetTheWholeCallAboveOneFullAttempt_forEveryService(ExternalWebService service,
      String perAttemptTimeoutProperty) {
    contextRunner.run(context -> {
      Duration wholeCallDeadline = timeLimiterConfig(context, service).getTimeoutDuration();
      Long perAttemptMillis = context.getEnvironment().getProperty(perAttemptTimeoutProperty, Long.class);

      assertThat(perAttemptMillis).isNotNull();
      assertThat(wholeCallDeadline).isGreaterThan(Duration.ofMillis(perAttemptMillis));
    });
  }

  @ParameterizedTest
  @EnumSource(value = ExternalWebService.class, names = {"MARKET_INVESTMENT_CATALOGUE", "BANK_OF_CANADA"})
  void shouldRetryMoreThanOnceAndBackOffExponentially_forEveryService(ExternalWebService service) {
    contextRunner.run(context -> {
      RetryConfig config = retryConfig(context, service);

      assertThat(config.getMaxAttempts()).isGreaterThan(1);
      assertThat(config.getIntervalBiFunction().apply(1, null))
          .isNotEqualTo(config.getIntervalBiFunction().apply(2, null));
    });
  }

  @ParameterizedTest
  @EnumSource(value = ExternalWebService.class, names = {"MARKET_INVESTMENT_CATALOGUE", "BANK_OF_CANADA"})
  void shouldRetryAndRecordServerErrorsOnly_forEveryService(ExternalWebService service) {
    contextRunner.run(context -> {
      WebClientResponseException serverError = responseException(HttpStatus.SERVICE_UNAVAILABLE);
      WebClientResponseException rejectedRequest = responseException(HttpStatus.NOT_FOUND);

      assertThat(retryConfig(context, service).getExceptionPredicate().test(serverError)).isTrue();
      assertThat(retryConfig(context, service).getExceptionPredicate().test(rejectedRequest)).isFalse();
      assertThat(circuitBreakerConfig(context, service).getRecordExceptionPredicate().test(serverError)).isTrue();
      assertThat(circuitBreakerConfig(context, service).getRecordExceptionPredicate().test(rejectedRequest)).isFalse();
    });
  }

  @ParameterizedTest
  @EnumSource(value = ExternalWebService.class, names = {"MARKET_INVESTMENT_CATALOGUE", "BANK_OF_CANADA"})
  void shouldRequireASampleBeforeOpeningAndReclosingOnItsOwn_forEveryService(ExternalWebService service) {
    contextRunner.run(context -> {
      CircuitBreakerConfig config = circuitBreakerConfig(context, service);

      assertThat(config.getMinimumNumberOfCalls()).isGreaterThan(1);
      assertThat(config.getFailureRateThreshold()).isBetween(1.0f, 99.0f);
      assertThat(config.getWaitIntervalFunctionInOpenState().apply(1)).isPositive();
      assertThat(config.isAutomaticTransitionFromOpenToHalfOpenEnabled()).isTrue();
      assertThat(config.getSlowCallDurationThreshold()).isLessThanOrEqualTo(Duration.ofSeconds(30));
    });
  }

  @Test
  void shouldScaleTheBankOfCanadaWindowToItsOwnTraffic_ratherThanFallBackToTheSharedDefault() {
    contextRunner.run(context -> {
      CircuitBreakerConfig bankOfCanada = circuitBreakerConfig(context, ExternalWebService.BANK_OF_CANADA);
      CircuitBreakerConfig marketInvestmentCatalogue = circuitBreakerConfig(context,
          ExternalWebService.MARKET_INVESTMENT_CATALOGUE);

      assertThat(bankOfCanada.getSlidingWindowSize()).isLessThan(marketInvestmentCatalogue.getSlidingWindowSize());
      assertThat(bankOfCanada.getMinimumNumberOfCalls()).isLessThan(marketInvestmentCatalogue
          .getMinimumNumberOfCalls());
    });
  }

  @Test
  void shouldExposeOneResilienceBeanPerService() {
    contextRunner.run(context -> assertThat(context.getBeansOfType(ExternalCallResilience.class)).hasSize(2));
  }

  private static CircuitBreakerConfig circuitBreakerConfig(AssertableApplicationContext context,
      ExternalWebService service) {
    return context.getBean(CircuitBreakerRegistry.class)
        .circuitBreaker(ExternalCallResilience.instanceName(service))
        .getCircuitBreakerConfig();
  }

  private static RetryConfig retryConfig(AssertableApplicationContext context, ExternalWebService service) {
    return context.getBean(RetryRegistry.class)
        .retry(ExternalCallResilience.instanceName(service))
        .getRetryConfig();
  }

  private static TimeLimiterConfig timeLimiterConfig(AssertableApplicationContext context,
      ExternalWebService service) {
    return context.getBean(TimeLimiterRegistry.class)
        .timeLimiter(ExternalCallResilience.instanceName(service))
        .getTimeLimiterConfig();
  }

  private static WebClientResponseException responseException(HttpStatus status) {
    return WebClientResponseException.create(status.value(), status.getReasonPhrase(),
        HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
  }
}
