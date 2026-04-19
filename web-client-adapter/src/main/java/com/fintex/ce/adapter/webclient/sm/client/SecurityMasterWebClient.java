package com.fintex.ce.adapter.webclient.sm.client;

import com.fintex.ce.model.error.exceptions.ExternalServiceBadResponseException;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

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
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
@RequiredArgsConstructor
public class SecurityMasterWebClient {

  private static final String SERVICE_NAME = "Security Master";

  private final WebClient smWebClient;

  /**
   * Performs a POST request and returns the response body.
   */
  public <T, R> R post(String path, T request, ParameterizedTypeReference<R> responseType) {
    log.debug("POST request to: {}", path);
    R result = smWebClient.post()
        .uri(path)
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response))
        .bodyToMono(responseType)
        .onErrorMap(SecurityMasterWebClient::handleError)
        .block();
    log.debug("POST response from: {} - status OK", path);
    return result;
  }

  /**
   * Performs a GET request and returns the response body.
   */
  public <R> R get(String path, Class<R> responseType) {
    log.debug("GET request to: {}", path);
    R result = smWebClient.get()
        .uri(path)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response))
        .bodyToMono(responseType)
        .onErrorMap(SecurityMasterWebClient::handleError)
        .block();
    log.debug("GET response from: {} - status OK", path);
    return result;
  }

  /**
   * Performs a GET request and returns the response body with parameterized type.
   */
  public <R> R get(String path, ParameterizedTypeReference<R> responseType) {
    log.debug("GET request to: {}", path);
    R result = smWebClient.get()
        .uri(path)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response))
        .bodyToMono(responseType)
        .onErrorMap(SecurityMasterWebClient::handleError)
        .block();
    log.debug("GET response from: {} - status OK", path);
    return result;
  }

  private Mono<Throwable> handleErrorResponse(String path, ClientResponse response) {
    HttpStatusCode status = response.statusCode();
    return response.bodyToMono(String.class)
        .defaultIfEmpty("")
        .flatMap(body -> {
          log.error("Security Master API error: {} {} - {}", path, status, body);
          return Mono.error(status.is4xxClientError()
              ? new ExternalServiceBadResponseException(SERVICE_NAME)
              : new ExternalServiceUnavailableException(SERVICE_NAME));
        });
  }

  private static Throwable handleError(Throwable error) {
    return error instanceof ExternalServiceBadResponseException || error instanceof ExternalServiceUnavailableException
        ? error
        : new ExternalServiceUnavailableException(SERVICE_NAME, error);
  }
}
