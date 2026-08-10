package com.fintex.ce.adapter.webclient.boc.client;

import com.fintex.ce.adapter.webclient.observability.ResponseItemCount;
import com.fintex.ce.model.error.exceptions.ExternalServiceBadResponseException;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;
import com.fintex.ce.port.observability.ExternalCallObservability;
import com.fintex.ce.port.observability.ExternalCallObservability.ExternalCall;
import com.fintex.ce.port.observability.ExternalService;

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
 *
 * <p>
 * Every call is reported to {@link ExternalCallObservability}. The outcome is filed by this class rather than by the
 * fetchers above it, so that no call can go unreported, and the upstream status is filed from the one place that sees
 * the raw response — the error handler — before it is mapped to a domain exception.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankOfCanadaWebClient {

  private static final String SERVICE_NAME = "Bank of Canada";
  private static final String GET = HttpMethod.GET.name();

  private final WebClient bocWebClient;
  private final ExternalCallObservability observability;

  public <R> R get(String path, Class<R> responseType) {
    ExternalCall call = observability.start(ExternalService.BANK_OF_CANADA, GET, path);
    try {
      log.debug("GET request to Bank of Canada: {}", path);
      R result = bocWebClient.get()
          .uri(path)
          .retrieve()
          .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response, call))
          .bodyToMono(responseType)
          .onErrorMap(BankOfCanadaWebClient::handleError)
          .block();
      log.debug("GET response from Bank of Canada: {} - status OK", path);
      call.completed(ResponseItemCount.of(result));
      return result;
    } catch (RuntimeException exception) {
      call.failed(exception);
      throw exception;
    }
  }

  private static Mono<Throwable> handleErrorResponse(String path, ClientResponse response, ExternalCall call) {
    HttpStatusCode status = response.statusCode();
    return response.bodyToMono(String.class)
        .defaultIfEmpty("")
        .flatMap(body -> {
          log.error("Bank of Canada API error: {} {} - {}", path, status, body);
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
