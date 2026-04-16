package com.fintex.ce.adapter.webclient.boc.client;

import com.fintex.ce.adapter.webclient.boc.exception.BankOfCanadaWebClientException;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankOfCanadaWebClient {

  private final WebClient bocWebClient;

  public <R> R get(String path, Class<R> responseType) {
    log.debug("GET request to Bank of Canada: {}", path);
    R result = bocWebClient.get()
        .uri(path)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> handleErrorResponse(path, response))
        .bodyToMono(responseType)
        .block();
    log.debug("GET response from Bank of Canada: {} - status OK", path);
    return result;
  }

  private Mono<Throwable> handleErrorResponse(String path, ClientResponse response) {
    return response.bodyToMono(String.class)
        .flatMap(body -> {
          log.error("Bank of Canada API error: {} {} - {}", path, response.statusCode(), body);
          return Mono.error(new BankOfCanadaWebClientException(response.statusCode(), body));
        });
  }
}