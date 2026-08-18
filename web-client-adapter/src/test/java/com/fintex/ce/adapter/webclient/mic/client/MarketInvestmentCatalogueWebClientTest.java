package com.fintex.ce.adapter.webclient.mic.client;

import com.fintex.ce.adapter.webclient.resilience.ExternalCallResilience;
import com.fintex.ce.adapter.webclient.resilience.TransientCallFailures;
import com.fintex.ce.model.error.exceptions.ExternalServiceBadResponseException;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;
import com.fintex.ce.port.observability.ExternalCallObservability;
import com.fintex.wm.commons.domain.ExternalWebService;

import org.springframework.web.reactive.function.client.WebClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Drives the client through the failure modes the resilience layer exists for, against a real socket: a transient
 * server error must be retried until it succeeds or the attempts are spent, a client error must fail straight through
 * without a second request, and an open circuit breaker must reject the call before any request leaves the process.
 * Each scenario also pins what the observability port sees: one outcome per logical call, never one per attempt.
 */
class MarketInvestmentCatalogueWebClientTest {

  private static final String PATH = "/api/v1/wealth/securities/attributes";
  private static final int MAX_ATTEMPTS = 3;

  private MockWebServer server;
  private WebClient webClient;
  private CircuitBreaker circuitBreaker;
  private Retry retry;
  private RecordingObservability observability;
  private MarketInvestmentCatalogueWebClient client;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    MarketInvestmentCatalogueRestProperties properties = new MarketInvestmentCatalogueRestProperties();
    properties.setBaseUrl(server.url("/").toString());
    properties.setTimeout(5000);
    webClient = new MarketInvestmentCatalogueWebClientConfig().micWebClient(WebClient.builder(), properties);
    circuitBreaker = CircuitBreaker.of(ExternalWebService.MARKET_INVESTMENT_CATALOGUE.name(), CircuitBreakerConfig
        .custom()
        .recordException(TransientCallFailures::isTransient)
        .build());
    retry = Retry.of(ExternalWebService.MARKET_INVESTMENT_CATALOGUE.name(), RetryConfig.custom()
        .maxAttempts(MAX_ATTEMPTS)
        .waitDuration(Duration.ofMillis(1))
        .retryOnException(TransientCallFailures::isTransient)
        .build());
    observability = new RecordingObservability();
    client = buildClient(Duration.ofSeconds(5));
  }

  private MarketInvestmentCatalogueWebClient buildClient(Duration wholeCallDeadline) {
    TimeLimiter timeLimiter = TimeLimiter.of(ExternalWebService.MARKET_INVESTMENT_CATALOGUE.name(),
        TimeLimiterConfig.custom().timeoutDuration(wholeCallDeadline).build());
    return new MarketInvestmentCatalogueWebClient(webClient,
        new ExternalCallResilience(circuitBreaker, retry, timeLimiter), observability);
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void shouldReturnBodyAfterRetrying_whenServerErrorPrecedesSuccess() {
    enqueueServerErrors(MAX_ATTEMPTS - 1);
    enqueueJson("{\"key\":\"value\"}");

    Map<?, ?> result = client.get(PATH, Map.class);

    assertThat(result).isEqualTo(Map.of("key", "value"));
    assertThat(server.getRequestCount()).isEqualTo(MAX_ATTEMPTS);
    assertThat(observability.outcomes)
        .as("retries belong to one logical call, so exactly one successful outcome is filed")
        .containsExactly("completed:1");
  }

  @Test
  void shouldThrowUnavailable_whenServerErrorOutlastsEveryAttempt() {
    enqueueServerErrors(MAX_ATTEMPTS);

    assertThatThrownBy(() -> client.get(PATH, Map.class))
        .isInstanceOf(ExternalServiceUnavailableException.class);

    assertThat(server.getRequestCount()).isEqualTo(MAX_ATTEMPTS);
    assertThat(observability.outcomes.getFirst())
        .as("the upstream status is filed once, for the final outcome, before the domain exception propagates")
        .isEqualTo("httpFailed:503");
  }

  @Test
  void shouldThrowBadResponseWithoutRetrying_whenProviderRejectsTheRequest() {
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"error\":\"bad request\"}"));

    assertThatThrownBy(() -> client.get(PATH, Map.class))
        .isInstanceOf(ExternalServiceBadResponseException.class);

    assertThat(server.getRequestCount())
        .as("a rejected request is this service's own bug, so repeating it cannot help")
        .isEqualTo(1);
    assertThat(observability.outcomes.getFirst()).isEqualTo("httpFailed:400");
  }

  @Test
  void shouldThrowUnavailableWithoutCallingProvider_whenCircuitBreakerIsOpen() {
    circuitBreaker.transitionToOpenState();

    assertThatThrownBy(() -> client.get(PATH, Map.class))
        .isInstanceOf(ExternalServiceUnavailableException.class);

    assertThat(server.getRequestCount())
        .as("an open breaker must shed load, not forward it")
        .isZero();
    assertThat(observability.outcomes.getFirst()).startsWith("failed:");
  }

  @Test
  void shouldThrowUnavailableWithoutAnotherAttempt_whenWholeCallDeadlineExpires() {
    client = buildClient(Duration.ofMillis(250));
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("{\"key\":\"value\"}")
        .setBodyDelay(2, TimeUnit.SECONDS));

    assertThatThrownBy(() -> client.get(PATH, Map.class))
        .isInstanceOf(ExternalServiceUnavailableException.class);

    assertThat(server.getRequestCount())
        .as("the deadline caps the logical call, so the cancelled attempt is not followed by another")
        .isEqualTo(1);
    assertThat(observability.outcomes.getFirst()).startsWith("failed:");
  }

  private void enqueueServerErrors(int count) {
    for (int i = 0; i < count; i++) {
      server.enqueue(new MockResponse().setResponseCode(503).setBody("{\"error\":\"unavailable\"}"));
    }
  }

  private void enqueueJson(String body) {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body));
  }

  private static final class RecordingObservability implements ExternalCallObservability {

    private final List<String> outcomes = new ArrayList<>();

    @Override
    public ExternalCall start(ExternalWebService service, String httpMethod, String endpoint) {
      return new ExternalCall() {

        @Override
        public void completed(int itemCount) {
          outcomes.add("completed:" + itemCount);
        }

        @Override
        public void failed(Throwable cause) {
          outcomes.add("failed:" + cause.getClass().getSimpleName());
        }

        @Override
        public void httpFailed(int statusCode, Throwable cause) {
          outcomes.add("httpFailed:" + statusCode);
        }
      };
    }
  }
}
