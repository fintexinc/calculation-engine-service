package ca.tangerine.pce.webclient.boc.client;

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

import ca.tangerine.pce.model.error.exceptions.ExternalServiceBadResponseException;
import ca.tangerine.pce.model.error.exceptions.ExternalServiceUnavailableException;
import ca.tangerine.pce.port.observability.ExternalCallObservability;
import ca.tangerine.pce.webclient.resilience.ExternalCallResilience;
import ca.tangerine.pce.webclient.resilience.TransientCallFailures;
import ca.tangerine.wm.commons.domain.ExternalWebService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Drives the client through the failure modes the resilience layer exists for, against a real socket: a transient
 * server error must be retried until it succeeds or the attempts are spent, a client error must fail straight through
 * without a second request, and an open circuit breaker must reject the call before any request leaves the process.
 * Each scenario also pins what the observability port sees: one outcome per logical call, never one per attempt.
 *
 * <p>
 * The same scenarios are pinned for Market Investment Catalogue. Both clients wrap one shared decoration and one shared
 * error mapper, so a change that quietly protects only the provider that happens to be covered is the failure this
 * class exists to prevent.
 */
class BankOfCanadaWebClientTest {

  private static final String PATH = "/observations/FXCADUSD/json";
  private static final int MAX_ATTEMPTS = 3;
  private static final Duration DEFAULT_WHOLE_CALL_DEADLINE = Duration.ofSeconds(30);

  private MockWebServer server;
  private WebClient webClient;
  private CircuitBreaker circuitBreaker;
  private Retry retry;
  private RecordingObservability observability;
  private BankOfCanadaWebClient client;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    BankOfCanadaProperties properties = new BankOfCanadaProperties();
    properties.setBaseUrl(server.url("/").toString());
    properties.setTimeout(5000);
    webClient = new BankOfCanadaWebClientConfig().bocWebClient(WebClient.builder(), properties);
    circuitBreaker = CircuitBreaker.of(ExternalWebService.BANK_OF_CANADA.name(), CircuitBreakerConfig.custom()
        .recordException(TransientCallFailures::isTransient)
        .build());
    retry = Retry.of(ExternalWebService.BANK_OF_CANADA.name(), RetryConfig.custom()
        .maxAttempts(MAX_ATTEMPTS)
        .waitDuration(Duration.ofMillis(1))
        .retryOnException(TransientCallFailures::isTransient)
        .build());
    observability = new RecordingObservability();
    client = buildClient(DEFAULT_WHOLE_CALL_DEADLINE);
  }

  private BankOfCanadaWebClient buildClient(Duration wholeCallDeadline) {
    TimeLimiter timeLimiter = TimeLimiter.of(ExternalWebService.BANK_OF_CANADA.name(),
        TimeLimiterConfig.custom().timeoutDuration(wholeCallDeadline).build());
    return new BankOfCanadaWebClient(webClient,
        new ExternalCallResilience(circuitBreaker, retry, timeLimiter), observability);
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void shouldReturnBodyAfterRetrying_whenServerErrorPrecedesSuccess() {
    enqueueServerErrors(MAX_ATTEMPTS - 1);
    enqueueJson("{\"observations\":[]}");

    Map<?, ?> result = client.get(PATH, Map.class);

    assertThat(result).isEqualTo(Map.of("observations", List.of()));
    assertThat(server.getRequestCount()).isEqualTo(MAX_ATTEMPTS);
    assertThat(observability.outcomes)
        .as("retries belong to one logical call, so exactly one successful outcome is filed")
        .containsExactly("completed:1");
  }

  @Test
  void shouldThrowUnavailable_whenServerErrorOutlastsEveryAttempt() {
    enqueueServerErrors(MAX_ATTEMPTS);

    assertThatThrownBy(() -> client.get(PATH, Map.class))
        .isInstanceOf(ExternalServiceUnavailableException.class)
        .hasMessageContaining(ExternalWebService.BANK_OF_CANADA.displayName());

    assertThat(server.getRequestCount()).isEqualTo(MAX_ATTEMPTS);
    assertThat(observability.outcomes.getFirst())
        .as("the upstream status is filed once, for the final outcome, before the domain exception propagates")
        .isEqualTo("httpFailed:503");
  }

  @Test
  void shouldThrowBadResponseWithoutRetrying_whenProviderRejectsTheRequest() {
    server.enqueue(new MockResponse().setResponseCode(404).setBody("{\"message\":\"series not found\"}"));

    assertThatThrownBy(() -> client.get(PATH, Map.class))
        .isInstanceOf(ExternalServiceBadResponseException.class)
        .hasMessageContaining(ExternalWebService.BANK_OF_CANADA.displayName());

    assertThat(server.getRequestCount())
        .as("a rejected request is this service's own bug, so repeating it cannot help")
        .isEqualTo(1);
    assertThat(observability.outcomes.getFirst()).isEqualTo("httpFailed:404");
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
  void shouldThrowUnavailableWithoutRetrying_whenTheResponseCannotBeDecoded() {
    enqueueJson("{\"observations\":");

    assertThatThrownBy(() -> client.get(PATH, Map.class))
        .isInstanceOf(ExternalServiceUnavailableException.class);

    assertThat(server.getRequestCount())
        .as("a body that cannot parse will not parse on a second fetch either")
        .isEqualTo(1);
    assertThat(observability.outcomes.getFirst()).startsWith("failed:");
  }

  @Test
  void shouldThrowUnavailableWithoutAnotherAttempt_whenWholeCallDeadlineExpires() {
    client = buildClient(Duration.ofMillis(250));
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("{\"observations\":[]}")
        .setBodyDelay(2, TimeUnit.SECONDS));

    assertThatThrownBy(() -> client.get(PATH, Map.class))
        .isInstanceOf(ExternalServiceUnavailableException.class);

    assertThat(server.getRequestCount())
        .as("the deadline caps the logical call, so it never reaches a second provider request")
        .isLessThanOrEqualTo(1);
    assertThat(observability.outcomes.getFirst()).startsWith("failed:");
  }

  private void enqueueServerErrors(int count) {
    for (int i = 0; i < count; i++) {
      server.enqueue(new MockResponse().setResponseCode(503).setBody("{\"message\":\"unavailable\"}"));
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
