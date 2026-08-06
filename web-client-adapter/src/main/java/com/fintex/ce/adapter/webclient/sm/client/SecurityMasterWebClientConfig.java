package com.fintex.ce.adapter.webclient.sm.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import org.slf4j.MDC;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Slf4j
@Configuration
@EnableConfigurationProperties(SecurityMasterRestProperties.class)
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class SecurityMasterWebClientConfig {

  static final String REQUEST_ID_HEADER = "X-Request-ID";
  static final String REQUEST_ID_MDC_KEY = "requestId";

  @Bean
  public WebClient smWebClient(
      WebClient.Builder webClientBuilder,
      SecurityMasterRestProperties properties) {

    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMillis(properties.getTimeout()))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getTimeout());

    if (properties.isLogBody()) {
      httpClient = httpClient.wiretap(
          "reactor.netty.http.client.HttpClient", LogLevel.TRACE, AdvancedByteBufFormat.TEXTUAL);
    }

    WebClient.Builder builder = webClientBuilder
        .baseUrl(properties.getBaseUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filter(propagateRequestId());

    if (properties.isLogRequests()) {
      builder = builder.filter(logRequest()).filter(logResponse());
    }

    return builder.build();
  }

  /**
   * Forwards the inbound {@code requestId} to Security Master so both services log the same identifier. The W3C
   * {@code traceparent} header is propagated separately by the Micrometer/OpenTelemetry instrumentation of the
   * auto-configured {@link WebClient.Builder}.
   */
  private ExchangeFilterFunction propagateRequestId() {
    return ExchangeFilterFunction.ofRequestProcessor(request -> {
      String requestId = MDC.get(REQUEST_ID_MDC_KEY);
      if (requestId == null || requestId.isBlank() || request.headers().containsKey(REQUEST_ID_HEADER)) {
        return Mono.just(request);
      }
      return Mono.just(ClientRequest.from(request).header(REQUEST_ID_HEADER, requestId).build());
    });
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(request -> {
      log.debug(">>> SM Request: {} {}", request.method(), request.url());
      return Mono.just(request);
    });
  }

  private ExchangeFilterFunction logResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(response -> {
      log.debug("<<< SM Response: {} - Status: {}", response.request().getURI(), response.statusCode());
      return Mono.just(response);
    });
  }
}
