package ca.tangerine.pce.webclient.resilience;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.micrometer.context.ContextRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

import reactor.core.publisher.Mono;

class ExternalCallResilienceTest {

  private static final String TRACE_ID_KEY = "traceId";
  private static final String TRACE_ID = "78c4d6e3f0e54761b4c2a32865a4dc30";
  private static final String RESPONSE = "response";
  private static final int MAX_ATTEMPTS = 3;
  private static final ThreadLocal<String> CURRENT_TRACE_ID = new ThreadLocal<>();

  @BeforeAll
  static void registerTraceIdAccessor() {
    ContextRegistry.getInstance().registerThreadLocalAccessor(
        TRACE_ID_KEY, CURRENT_TRACE_ID::get, CURRENT_TRACE_ID::set, CURRENT_TRACE_ID::remove);
  }

  @AfterAll
  static void removeTraceIdAccessor() {
    ContextRegistry.getInstance().removeThreadLocalAccessor(TRACE_ID_KEY);
  }

  @AfterEach
  void clearTraceId() {
    CURRENT_TRACE_ID.remove();
  }

  @Test
  void shouldKeepCapturedTraceId_whenSecondAndThirdAttemptsAreRetried() {
    ExternalCallResilience resilience = resilience(MAX_ATTEMPTS);
    AtomicInteger attempts = new AtomicInteger();
    List<String> traceIds = new ArrayList<>();
    CURRENT_TRACE_ID.set(TRACE_ID);
    Mono<String> call = Mono.deferContextual(context -> {
      traceIds.add(context.getOrDefault(TRACE_ID_KEY, null));
      return attempts.incrementAndGet() < MAX_ATTEMPTS
          ? Mono.error(new IllegalStateException("transient failure"))
          : Mono.just(RESPONSE);
    });

    String response = resilience.decorate(call).block();

    assertThat(response).isEqualTo(RESPONSE);
    assertThat(attempts).hasValue(MAX_ATTEMPTS);
    assertThat(traceIds).containsExactly(TRACE_ID, TRACE_ID, TRACE_ID);
  }

  private static ExternalCallResilience resilience(int maxAttempts) {
    String instance = "test";
    Retry retry = Retry.of(instance, RetryConfig.custom()
        .maxAttempts(maxAttempts)
        .waitDuration(Duration.ofMillis(1))
        .build());
    return new ExternalCallResilience(
        CircuitBreaker.ofDefaults(instance), retry, TimeLimiter.ofDefaults(instance));
  }
}
