package com.fintex.ce.adapter.webclient.sm.client;

import com.fintex.ce.adapter.webclient.observability.ResponseItemCount;
import com.fintex.ce.model.error.exceptions.ExternalServiceBadResponseException;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;
import com.fintex.ce.port.observability.ExternalCallObservability;
import com.fintex.ce.port.observability.ExternalCallObservability.ExternalCall;
import com.fintex.ce.port.observability.ExternalService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
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
 * Failures are split by cause:
 * <ul>
 * <li>4xx response → {@link ExternalServiceBadResponseException} (HTTP 502 Bad Gateway)</li>
 * <li>5xx response or transport/connection error → {@link ExternalServiceUnavailableException} (HTTP 503 Service
 * Unavailable)</li>
 * </ul>
 *
 * <p>
 * Every call is reported to {@link ExternalCallObservability}. The outcome is filed by this class rather than by the
 * fetchers above it, so that no call can go unreported, and the upstream status is filed from the one place that sees
 * the raw response — the error handler — before it is mapped to a domain exception.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
@RequiredArgsConstructor
public class SecurityMasterWebClient {

  private static final String SERVICE_NAME = "Security Master";
  private static final String GET = HttpMethod.GET.name();
  private static final String POST = HttpMethod.POST.name();

  private final WebClient smWebClient;
  private final ExternalCallObservability observability;

  /**
   * Performs a POST request and returns the response body.
   */
  public <T, R> R post(String path, T request, ParameterizedTypeReference<R> responseType) {
    return observe(POST, path, call -> {
      log.debug("POST request to: {}", path);
      R result = smWebClient.post()
          .uri(path)
          .bodyValue(request)
          .retrieve()
          .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response, call))
          .bodyToMono(responseType)
          .onErrorMap(SecurityMasterWebClient::handleError)
          .block();
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
      R result = smWebClient.get()
          .uri(uriBuilder -> buildUri(uriBuilder, path, queryParams))
          .retrieve()
          .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response, call))
          .bodyToMono(responseType)
          .onErrorMap(SecurityMasterWebClient::handleError)
          .block();
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
      R result = smWebClient.get()
          .uri(path)
          .retrieve()
          .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response, call))
          .bodyToMono(responseType)
          .onErrorMap(SecurityMasterWebClient::handleError)
          .block();
      log.debug("GET response from: {} - status OK", path);
      return result;
    });
  }

  private <R> R observe(String httpMethod, String path, Function<ExternalCall, R> action) {
    ExternalCall call = observability.start(ExternalService.SECURITY_MASTER, httpMethod, path);
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

  private static Mono<Throwable> handleErrorResponse(String path, ClientResponse response, ExternalCall call) {
    HttpStatusCode status = response.statusCode();
    return response.bodyToMono(String.class)
        .defaultIfEmpty("")
        .flatMap(body -> {
          log.error("Security Master API error: {} {} - {}", path, status, body);
          Throwable error = status.is4xxClientError()
              ? new ExternalServiceBadResponseException(SERVICE_NAME)
              : new ExternalServiceUnavailableException(SERVICE_NAME);
          call.httpFailed(status.value(), error);
          return Mono.error(error);
        });
  }

  private static Throwable handleError(Throwable error) {
    return error instanceof ExternalServiceBadResponseException || error instanceof ExternalServiceUnavailableException
        ? error
        : new ExternalServiceUnavailableException(SERVICE_NAME, error);
  }
}
