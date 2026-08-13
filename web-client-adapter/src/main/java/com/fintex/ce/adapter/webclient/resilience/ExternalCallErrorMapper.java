package com.fintex.ce.adapter.webclient.resilience;

import com.fintex.ce.model.error.exceptions.ExternalServiceBadResponseException;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;
import com.fintex.ce.port.observability.ExternalCallObservability.ExternalCall;
import com.fintex.wm.commons.domain.ExternalWebService;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import reactor.core.Exceptions;

/**
 * Maps the final failure of a resilience-decorated outbound call to the domain exception the rest of the service
 * understands, and files the upstream status with the call's observability record from the one place that still sees
 * the raw response.
 *
 * <p>
 * Applied outside the retry and circuit breaker operators, so it runs exactly once per logical call — after the
 * attempts are spent or the breaker refused the call — rather than once per attempt. A 4xx response says the request
 * itself was wrong and maps to {@link ExternalServiceBadResponseException}; a 5xx response, a transport failure or an
 * open breaker all mean the provider cannot serve right now and map to {@link ExternalServiceUnavailableException}.
 *
 * <p>
 * The failure is classified in one place and then fails the caller, because a calculation cannot be served from partial
 * data. This is the deliberate counterpart to the Security Master Service, which classifies the same causes in one
 * place and then answers with an empty result, because an import has to survive one provider being unavailable.
 *
 * <p>
 * The cause is unwrapped first, so the same classification applies whether it arrives through a reactive operator or is
 * caught around a blocking subscription.
 */
@Slf4j
@UtilityClass
public class ExternalCallErrorMapper {

  public static Throwable toDomainError(ExternalWebService service, String path, Throwable error, ExternalCall call) {
    String serviceName = service.displayName();
    Throwable cause = Exceptions.unwrap(error);

    if (cause instanceof ExternalServiceBadResponseException
        || cause instanceof ExternalServiceUnavailableException) {
      return cause;
    }
    if (cause instanceof WebClientResponseException response) {
      log.error("{} API error: {} {} - {}", serviceName, path, response.getStatusCode(),
          response.getResponseBodyAsString());
      Throwable mapped = response.getStatusCode().is4xxClientError()
          ? new ExternalServiceBadResponseException(serviceName)
          : new ExternalServiceUnavailableException(serviceName);
      call.httpFailed(response.getStatusCode().value(), mapped);
      return mapped;
    }
    if (cause instanceof CallNotPermittedException rejected) {
      log.error("Circuit breaker open for {}, {} not called: {}", serviceName, path, rejected.getMessage());
      return new ExternalServiceUnavailableException(serviceName, rejected);
    }
    log.error("{} call failed: {} - {}", serviceName, path, cause.toString());
    return new ExternalServiceUnavailableException(serviceName, cause);
  }
}
