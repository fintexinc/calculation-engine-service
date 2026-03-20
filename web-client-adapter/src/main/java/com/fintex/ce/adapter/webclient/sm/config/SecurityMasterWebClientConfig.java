package com.fintex.ce.adapter.webclient.sm.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class SecurityMasterWebClientConfig {

  @Bean
  public WebClient smWebClient(
      WebClient.Builder webClientBuilder,
      @Value("${external-services.security-master.rest.base-url}") String baseUrl,
      @Value("${external-services.security-master.rest.timeout:90000}") int timeout) {

    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMillis(timeout))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);

    return webClientBuilder
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}
