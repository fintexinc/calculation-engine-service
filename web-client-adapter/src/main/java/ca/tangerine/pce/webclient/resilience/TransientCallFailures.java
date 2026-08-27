package ca.tangerine.pce.webclient.resilience;

import org.springframework.core.codec.CodecException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * Decides which outbound-call failures are worth retrying and worth counting against a circuit breaker: the ones a
 * provider can plausibly recover from on its own — a transport or timeout failure, a server error, a throttle or a
 * request timeout it reported itself.
 *
 * <p>
 * Everything else is deliberately excluded. A 4xx other than 408 and 429 says the request itself was wrong, so
 * repeating it cannot help and letting it trip the breaker would take a healthy provider out of service over this
 * service's own bug. A rejection by an already-open breaker never reached the provider at all, so it is neither retried
 * nor recorded as a provider failure.
 *
 * <p>
 * A response that could not be decoded is the same story as a rejected request, and is tested for before the transport
 * failures on purpose: the codec reports it as a {@link CodecException} wrapping the parser's own failure, and
 * Jackson's is an {@link IOException}, so checking the transport first would classify every malformed payload as a
 * transient failure and spend all its attempts re-fetching a body that cannot parse.
 *
 * <p>
 * Referenced from configuration as both {@code record-failure-predicate} and {@code retry-exception-predicate}, so the
 * breaker and the retry always agree on what a provider failure is. Resilience4j instantiates it reflectively, hence
 * the public no-argument constructor.
 */
public class TransientCallFailures implements Predicate<Throwable> {

  private static final int MAX_CAUSE_CHAIN_DEPTH = 10;

  private static final Set<Integer> RETRYABLE_CLIENT_ERRORS = Set.of(
      HttpStatus.REQUEST_TIMEOUT.value(),
      HttpStatus.TOO_MANY_REQUESTS.value());

  @Override
  public boolean test(Throwable throwable) {
    return isTransient(throwable);
  }

  public static boolean isTransient(Throwable throwable) {
    Throwable cause = throwable;
    for (int depth = 0; cause != null && depth < MAX_CAUSE_CHAIN_DEPTH; depth++) {
      if (cause instanceof CallNotPermittedException || cause instanceof CodecException) {
        return false;
      }
      if (cause instanceof WebClientResponseException response) {
        return isRetryableStatus(response.getStatusCode());
      }
      if (cause instanceof WebClientRequestException
          || cause instanceof IOException
          || cause instanceof TimeoutException) {
        return true;
      }
      if (cause.getCause() == cause) {
        return false;
      }
      cause = cause.getCause();
    }
    return false;
  }

  private static boolean isRetryableStatus(HttpStatusCode status) {
    return status.is5xxServerError() || RETRYABLE_CLIENT_ERRORS.contains(status.value());
  }
}
