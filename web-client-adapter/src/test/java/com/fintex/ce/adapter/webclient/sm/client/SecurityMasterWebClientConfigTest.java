package com.fintex.ce.adapter.webclient.sm.client;

import org.springframework.web.reactive.function.client.WebClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Covers the one thing that makes this service's logs correlatable with Security Master's: the inbound request id has
 * to leave on the outbound call. Driven through the real bean against a real socket, because the propagation happens in
 * an exchange filter and a unit test of the filter in isolation would not prove the bean actually installs it.
 */
class SecurityMasterWebClientConfigTest {

  private MockWebServer server;
  private WebClient webClient;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    SecurityMasterRestProperties properties = new SecurityMasterRestProperties();
    properties.setBaseUrl(server.url("/").toString());
    properties.setTimeout(5000);
    webClient = new SecurityMasterWebClientConfig().smWebClient(WebClient.builder(), properties);
  }

  @AfterEach
  void tearDown() throws IOException {
    MDC.remove(SecurityMasterWebClientConfig.REQUEST_ID_MDC_KEY);
    server.shutdown();
  }

  @Test
  void shouldForwardRequestId_whenInboundRequestIdIsInScope() throws InterruptedException {
    MDC.put(SecurityMasterWebClientConfig.REQUEST_ID_MDC_KEY, "abc-123");

    RecordedRequest recorded = exchange();

    assertThat(recorded.getHeader(SecurityMasterWebClientConfig.REQUEST_ID_HEADER))
        .as("both services must log the same identifier for one client request")
        .isEqualTo("abc-123");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void shouldSendNoRequestIdHeader_whenNoUsableRequestIdIsInScope(String requestId) throws InterruptedException {
    if (requestId == null) {
      MDC.remove(SecurityMasterWebClientConfig.REQUEST_ID_MDC_KEY);
    } else {
      MDC.put(SecurityMasterWebClientConfig.REQUEST_ID_MDC_KEY, requestId);
    }

    RecordedRequest recorded = exchange();

    assertThat(recorded.getHeader(SecurityMasterWebClientConfig.REQUEST_ID_HEADER))
        .as("a blank id is worse than no id — it looks like correlation while carrying nothing")
        .isNull();
  }

  @Test
  void shouldKeepTheCallersRequestId_whenTheRequestAlreadyCarriesOne() throws InterruptedException {
    MDC.put(SecurityMasterWebClientConfig.REQUEST_ID_MDC_KEY, "from-mdc");
    server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

    webClient.get()
        .uri("/api/v1/wealth/securities")
        .header(SecurityMasterWebClientConfig.REQUEST_ID_HEADER, "explicit")
        .retrieve()
        .toBodilessEntity()
        .block();

    assertThat(server.takeRequest().getHeaders().values(SecurityMasterWebClientConfig.REQUEST_ID_HEADER))
        .as("an explicit header must be neither replaced nor duplicated")
        .containsExactly("explicit");
  }

  @Test
  void shouldSendJsonContentType_whenRequestIsIssued() throws InterruptedException {
    assertThat(exchange().getHeader("Content-Type")).startsWith("application/json");
  }

  private RecordedRequest exchange() throws InterruptedException {
    server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    webClient.get()
        .uri("/api/v1/wealth/securities")
        .retrieve()
        .toBodilessEntity()
        .block();
    return server.takeRequest();
  }
}
