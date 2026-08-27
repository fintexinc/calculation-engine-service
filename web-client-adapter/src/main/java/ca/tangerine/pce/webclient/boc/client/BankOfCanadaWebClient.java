package ca.tangerine.pce.webclient.boc.client;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ca.tangerine.pce.model.error.exceptions.ExternalServiceBadResponseException;
import ca.tangerine.pce.model.error.exceptions.ExternalServiceUnavailableException;
import ca.tangerine.pce.port.observability.ExternalCallObservability;
import ca.tangerine.pce.port.observability.ExternalCallObservability.ExternalCall;
import ca.tangerine.pce.webclient.observability.ResponseItemCount;
import ca.tangerine.pce.webclient.resilience.ExternalCallErrorMapper;
import ca.tangerine.pce.webclient.resilience.ExternalCallResilience;
import ca.tangerine.wm.commons.domain.ExternalWebService;

/**
 * WebClient for Bank of Canada API calls. Every call runs through {@link ExternalCallResilience}: transient failures —
 * transport errors, timeouts, 5xx, 408 and 429 — are retried and counted against the circuit breaker, while any other
 * 4xx fails straight through as this service's own bug. Failures of the whole logical call are then split by cause:
 * <ul>
 * <li>4xx response → {@link ExternalServiceBadResponseException} (HTTP 502 Bad Gateway)</li>
 * <li>5xx response, transport/connection error or an open circuit breaker → {@link ExternalServiceUnavailableException}
 * (HTTP 503 Service Unavailable)</li>
 * </ul>
 *
 * <p>
 * Every call is reported to {@link ExternalCallObservability}. The outcome is filed by this class rather than by the
 * fetchers above it, so that no call can go unreported. The raw status stays visible until the resilience operators
 * have run out of attempts, so the mapping to a domain exception — and the filing of the upstream status — happens
 * exactly once, for the final outcome of the logical call rather than for every attempt.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankOfCanadaWebClient {

  private static final String GET = HttpMethod.GET.name();

  private final WebClient bocWebClient;
  private final ExternalCallResilience bankOfCanadaCallResilience;
  private final ExternalCallObservability observability;

  public <R> R get(String path, Class<R> responseType) {
    ExternalCall call = observability.start(ExternalWebService.BANK_OF_CANADA, GET, path);
    try {
      log.debug("GET request to Bank of Canada: {}", path);
      R result = bankOfCanadaCallResilience.decorate(bocWebClient.get()
          .uri(path)
          .retrieve()
          .bodyToMono(responseType))
          .onErrorMap(error -> ExternalCallErrorMapper.toDomainError(ExternalWebService.BANK_OF_CANADA, path, error,
              call))
          .block();
      log.debug("GET response from Bank of Canada: {} - status OK", path);
      call.completed(ResponseItemCount.of(result));
      return result;
    } catch (RuntimeException exception) {
      call.failed(exception);
      throw exception;
    }
  }

}
