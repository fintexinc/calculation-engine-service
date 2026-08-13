package com.fintex.ce.adapter.webclient.resilience;

import com.fintex.wm.commons.domain.ExternalWebService;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

/**
 * The retry, circuit breaker and whole-call deadline of one external service, applied together to a single outbound
 * call.
 *
 * <p>
 * The retry sits outside the breaker, the same order the Resilience4j aspects use: every attempt passes through the
 * breaker, so a provider that is failing trips it as fast as its own error rate warrants rather than at a third of that
 * rate. Once the breaker is open the retry does not fight it, because a rejection by an open breaker is not a transient
 * failure — see {@link TransientCallFailures}.
 *
 * <p>
 * The time limiter sits outside the retry and caps the logical call as a whole — every attempt plus the backoff between
 * them. The per-attempt response timeout on the HTTP client cannot do this job: it multiplies by the number of
 * attempts, so with it alone the worst case handed to the caller is that timeout times every retry. When the deadline
 * fires, the in-flight attempt is cancelled and the caller gets one {@code TimeoutException} for the whole call.
 *
 * <p>
 * The call is decorated as a cold {@link Mono} and re-subscribed on each attempt, which is what makes a retry an actual
 * second request rather than a replay of the first response. Whatever the caller wraps around the decorated chain —
 * error mapping, observability, fallbacks — therefore sees one logical call and its final outcome, not one event per
 * attempt.
 *
 * <p>
 * The Resilience4j instance carrying the thresholds is looked up by {@link ExternalWebService} rather than by a name
 * spelled out at the call site, so the configuration, the meters and the actuator all key on the same provider
 * vocabulary the rest of the service uses, and a provider cannot be given protection under a name nothing configures.
 */
@Slf4j
public final class ExternalCallResilience {

  private final CircuitBreaker circuitBreaker;
  private final Retry retry;
  private final TimeLimiter timeLimiter;

  public ExternalCallResilience(CircuitBreaker circuitBreaker, Retry retry, TimeLimiter timeLimiter) {
    this.circuitBreaker = circuitBreaker;
    this.retry = retry;
    this.timeLimiter = timeLimiter;
    logStateTransitions(circuitBreaker);
    logRetries(retry);
    logDeadlines(timeLimiter);
  }

  public static ExternalCallResilience of(ExternalWebService service, CircuitBreakerRegistry circuitBreakers,
      RetryRegistry retries, TimeLimiterRegistry timeLimiters) {
    String instance = instanceName(service);
    return new ExternalCallResilience(circuitBreakers.circuitBreaker(instance), retries.retry(instance),
        timeLimiters.timeLimiter(instance));
  }

  public static String instanceName(ExternalWebService service) {
    return service.name();
  }

  public <T> Mono<T> decorate(Mono<T> call) {
    return call
        .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
        .transformDeferred(RetryOperator.of(retry))
        .transformDeferred(TimeLimiterOperator.of(timeLimiter));
  }

  private static void logStateTransitions(CircuitBreaker circuitBreaker) {
    circuitBreaker.getEventPublisher().onStateTransition(event -> log.warn(
        "Circuit breaker '{}' moved from {} to {}; failure rate {}%, slow call rate {}%",
        event.getCircuitBreakerName(),
        event.getStateTransition().getFromState(),
        event.getStateTransition().getToState(),
        circuitBreaker.getMetrics().getFailureRate(),
        circuitBreaker.getMetrics().getSlowCallRate()));
  }

  private static void logRetries(Retry retry) {
    retry.getEventPublisher().onRetry(event -> log.warn(
        "Retrying '{}' after attempt {} failed, waiting {}: {}",
        event.getName(),
        event.getNumberOfRetryAttempts(),
        event.getWaitInterval(),
        String.valueOf(event.getLastThrowable())));
  }

  private static void logDeadlines(TimeLimiter timeLimiter) {
    timeLimiter.getEventPublisher().onTimeout(event -> log.warn(
        "Call to '{}' exceeded its whole-call deadline of {} and was cancelled",
        event.getTimeLimiterName(),
        timeLimiter.getTimeLimiterConfig().getTimeoutDuration()));
  }
}
