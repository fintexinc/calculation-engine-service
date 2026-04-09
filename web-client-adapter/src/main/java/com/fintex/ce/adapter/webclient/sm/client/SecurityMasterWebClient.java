package com.fintex.ce.adapter.webclient.sm.client;

import com.fintex.ce.adapter.webclient.sm.exception.SecurityMasterWebClientException;

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
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
@RequiredArgsConstructor
public class SecurityMasterWebClient {

  private final WebClient smWebClient;

  /**
   * Performs a POST request and returns the response body.
   *
   * @param path
   *          endpoint path
   * @param request
   *          request body
   * @param responseType
   *          parameterized type reference for response
   * @param <T>
   *          request type
   * @param <R>
   *          response type
   * @return response body or null if empty
   */
  public <T, R> R post(String path, T request, ParameterizedTypeReference<R> responseType) {
    log.debug("POST request to: {}", path);
    R result = smWebClient.post()
        .uri(path)
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response))
        .bodyToMono(responseType)
        .block();
    log.debug("POST response from: {} - status OK", path);
    return result;
  }

  /**
   * Performs a GET request and returns the response body.
   *
   * @param path
   *          endpoint path
   * @param responseType
   *          response class type
   * @param <R>
   *          response type
   * @return response body or null if empty
   */
  public <R> R get(String path, Class<R> responseType) {
    log.debug("GET request to: {}", path);
    R result = smWebClient.get()
        .uri(path)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response))
        .bodyToMono(responseType)
        .block();
    log.debug("GET response from: {} - status OK", path);
    return result;
  }

  /**
   * Performs a GET request and returns the response body with parameterized type.
   *
   * @param path
   *          endpoint path
   * @param responseType
   *          parameterized type reference for response
   * @param <R>
   *          response type
   * @return response body or null if empty
   */
  public <R> R get(String path, ParameterizedTypeReference<R> responseType) {
    log.debug("GET request to: {}", path);
    R result = smWebClient.get()
        .uri(path)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response))
        .bodyToMono(responseType)
        .block();
    log.debug("GET response from: {} - status OK", path);
    return result;
  }

  private Mono<Throwable> handleErrorResponse(String path, ClientResponse response) {
    return response.bodyToMono(String.class)
        .flatMap(body -> {
          log.error("Security Master API error: {} {} - {}", path, response.statusCode(), body);
          return Mono.error(new SecurityMasterWebClientException(response.statusCode(), body));
        });
  }
}
