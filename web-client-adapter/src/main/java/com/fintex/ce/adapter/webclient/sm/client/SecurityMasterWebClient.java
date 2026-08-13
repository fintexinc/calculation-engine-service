package com.fintex.ce.adapter.webclient.sm.client;

import com.fintex.ce.adapter.webclient.observability.ResponseItemCount;
import com.fintex.ce.adapter.webclient.resilience.ExternalCallErrorMapper;
import com.fintex.ce.adapter.webclient.resilience.ExternalCallResilience;
import com.fintex.ce.model.error.exceptions.ExternalServiceBadResponseException;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;
import com.fintex.ce.port.observability.ExternalCallObservability;
import com.fintex.ce.port.observability.ExternalCallObservability.ExternalCall;
import com.fintex.wm.commons.domain.ExternalWebService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

/**
 * Generic WebClient for Security Master API calls. Provides generic HTTP methods that can be used by specific fetchers.
 * Every call runs through {@link ExternalCallResilience}: transient failures — transport errors, timeouts, 5xx, 408 and
 * 429 — are retried and counted against the circuit breaker, while any other 4xx fails straight through as this
 * service's own bug. Failures of the whole logical call are then split by cause:
 * <ul>
 * <li>4xx response → {@link ExternalServiceBadResponseException} (HTTP 502 Bad Gateway)</li>
 * <li>5xx response, transport/connection error or an open circuit breaker → {@link ExternalServiceUnavailableException}
 * (HTTP 503 Service Unavailable)</li>
 * </ul>
 *
 * <p>
 * Retries apply to POST as well as GET. That is safe here only because Security Master's POST endpoints are read-only
 * queries — POST carries a request body too large for a URL, it never mutates state. An endpoint that does mutate state
 * must not be called through this client without revisiting that assumption, since a retried mutation is a duplicate
 * one.
 *
 * <p>
 * Every call is reported to {@link ExternalCallObservability}. The outcome is filed by this class rather than by the
 * fetchers above it, so that no call can go unreported. The raw status stays visible until the resilience operators
 * have run out of attempts, so the mapping to a domain exception — and the filing of the upstream status — happens
 * exactly once, for the final outcome of the logical call rather than for every attempt.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
@RequiredArgsConstructor
public class SecurityMasterWebClient {

  private static final String GET = HttpMethod.GET.name();
  private static final String POST = HttpMethod.POST.name();

  private final WebClient smWebClient;
  private final ExternalCallResilience securityMasterCallResilience;
  private final ExternalCallObservability observability;

  /**
   * Performs a POST request and returns the response body.
   */
  public <T, R> R post(String path, T request, ParameterizedTypeReference<R> responseType) {
    return observe(POST, path, call -> {
      log.debug("POST request to: {}", path);
      R result = execute(path, call, smWebClient.post()
          .uri(path)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(responseType));
      log.debug("POST response from: {} - status OK", path);
      return result;
    });
  }

  /**
   * Performs a GET request and returns the response body.
   */
  public <R> R get(String path, Class<R> responseType) {
    return get(path, Map.of(), responseType);
  }

  /**
   * Performs a GET request with query parameters and returns the response body. Each map entry becomes a
   * {@code ?name=value} pair on the URL; Spring's {@link UriBuilder} handles encoding so callers must not pre-escape
   * values. Pass {@link Map#of()} for no params.
   */
  public <R> R get(String path, Map<String, ?> queryParams, Class<R> responseType) {
    return observe(GET, path, call -> {
      log.debug("GET request to: {} params={}", path, queryParams);
      R result = execute(path, call, smWebClient.get()
          .uri(uriBuilder -> buildUri(uriBuilder, path, queryParams))
          .retrieve()
          .bodyToMono(responseType));
      log.debug("GET response from: {} - status OK", path);
      return result;
    });
  }

  /**
   * Performs a GET request and returns the response body with parameterized type.
   */
  public <R> R get(String path, ParameterizedTypeReference<R> responseType) {
    return observe(GET, path, call -> {
      log.debug("GET request to: {}", path);
      R result = execute(path, call, smWebClient.get()
          .uri(path)
          .retrieve()
          .bodyToMono(responseType));
      log.debug("GET response from: {} - status OK", path);
      return result;
    });
  }

  private <R> R execute(String path, ExternalCall call, Mono<R> request) {
    return securityMasterCallResilience.decorate(request)
        .onErrorMap(error -> ExternalCallErrorMapper.toDomainError(ExternalWebService.SECURITY_MASTER, path, error,
            call))
        .block();
  }

  private <R> R observe(String httpMethod, String path, Function<ExternalCall, R> action) {
    ExternalCall call = observability.start(ExternalWebService.SECURITY_MASTER, httpMethod, path);
    try {
      R result = action.apply(call);
      call.completed(ResponseItemCount.of(result));
      return result;
    } catch (RuntimeException exception) {
      call.failed(exception);
      throw exception;
    }
  }

  private static URI buildUri(UriBuilder uriBuilder, String path, Map<String, ?> queryParams) {
    UriBuilder b = uriBuilder.path(path);
    queryParams.forEach((k, v) -> {
      if (v != null) {
        b.queryParam(k, v);
      }
    });
    return b.build();
  }

}
