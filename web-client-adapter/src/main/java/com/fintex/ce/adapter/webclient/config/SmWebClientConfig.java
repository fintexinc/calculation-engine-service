package com.fintex.ce.adapter.webclient.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "rest")
public class SmWebClientConfig {

  @Bean
  public WebClient smWebClient(
      @Value("${security-master.rest.base-url}") String baseUrl,
      @Value("${security-master.rest.timeout:90000}") int timeout) {
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMillis(timeout))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
    return WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}
