package com.fintex.ce.adapter.webclient.boc.client;

import com.fintex.ce.adapter.webclient.observability.ExternalServiceObservability;
import com.fintex.ce.model.error.exceptions.ExternalServiceBadResponseException;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

/**
 * WebClient for Bank of Canada API calls. Failures are split by cause:
 * <ul>
 * <li>4xx response → {@link ExternalServiceBadResponseException} (HTTP 502 Bad Gateway)</li>
 * <li>5xx response or transport/connection error → {@link ExternalServiceUnavailableException} (HTTP 503 Service
 * Unavailable)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankOfCanadaWebClient {

  private static final String SERVICE_NAME = "Bank of Canada";
  private static final String SERVICE_TAG_VALUE = "bank-of-canada";
  private static final String GET = HttpMethod.GET.name();

  private final WebClient bocWebClient;
  private final ExternalServiceObservability observability;

  public <R> R get(String path, Class<R> responseType) {
    return observability.observe(SERVICE_TAG_VALUE, GET, path, () -> {
      log.debug("GET request to Bank of Canada: {}", path);
      R result = bocWebClient.get()
          .uri(path)
          .retrieve()
          .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response))
          .bodyToMono(responseType)
          .onErrorMap(BankOfCanadaWebClient::handleError)
          .block();
      log.debug("GET response from Bank of Canada: {} - status OK", path);
      return result;
    });
  }

  private Mono<Throwable> handleErrorResponse(String path, ClientResponse response) {
    HttpStatusCode status = response.statusCode();
    return response.bodyToMono(String.class)
        .defaultIfEmpty("")
        .flatMap(body -> {
          log.error("Bank of Canada API error: {} {} - {}", path, status, body);
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
