package ca.tangerine.pce.webclient.boc.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Slf4j
@Configuration
@EnableConfigurationProperties(BankOfCanadaProperties.class)
public class BankOfCanadaWebClientConfig {

  @Bean
  public WebClient bocWebClient(
      WebClient.Builder webClientBuilder,
      BankOfCanadaProperties properties) {

    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMillis(properties.getTimeout()))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getTimeout());

    if (properties.isLogBody()) {
      httpClient = httpClient.wiretap(
          "reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
    }

    WebClient.Builder builder = webClientBuilder
        .baseUrl(properties.getBaseUrl())
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024));

    if (properties.isLogRequests()) {
      builder = builder.filter(logRequest()).filter(logResponse());
    }

    return builder.build();
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(request -> {
      log.debug(">>> BoC Request: {} {}", request.method(), request.url());
      return Mono.just(request);
    });
  }

  private ExchangeFilterFunction logResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(response -> {
      log.debug("<<< BoC Response: {} - Status: {}", response.request().getURI(), response.statusCode());
      return Mono.just(response);
    });
  }
}