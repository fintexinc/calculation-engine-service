package com.fintex.ce.adapter.webclient.client;

import com.fintex.ce.adapter.webclient.dto.SmAttributeRequest;
import com.fintex.ce.adapter.webclient.dto.SmAttributeResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "rest")
public class SmRestClient {

  private final WebClient webClient;

  public SmRestClient(WebClient smWebClient) {
    this.webClient = smWebClient;
  }

  public <T> List<SmAttributeResponse<T>> postAttributes(
      String path,
      SmAttributeRequest request,
      ParameterizedTypeReference<List<SmAttributeResponse<T>>> typeRef) {
    List<SmAttributeResponse<T>> result = webClient.post()
        .uri(path)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(typeRef)
        .block();
    return result != null ? result : List.of();
  }

  public <T> T getResource(String path, Class<T> clazz) {
    return webClient.get()
        .uri(path)
        .retrieve()
        .bodyToMono(clazz)
        .block();
  }
}
