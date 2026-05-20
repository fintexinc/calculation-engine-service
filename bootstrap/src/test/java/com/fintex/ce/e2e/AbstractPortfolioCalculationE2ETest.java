package com.fintex.ce.e2e;

import com.fintex.ce.PortfolioCalculationEngineApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodySpec;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

@Tag("e2e")
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractPortfolioCalculationE2ETest {

  private static final String basePath = "/api/v1/portfolio/calculations";

  protected static MockWebServer smsMockServer;

  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

  @Autowired
  protected WebTestClient webTestClient;

  protected abstract String metricPath();

  protected abstract String requestBodyForSmsUnavailableScenario();

  protected abstract String requestBodyForPositiveSmsScenario();

  protected abstract String smsPositiveResponseBody();

  protected abstract String requestBodyForMismatchedMetricScenario();

  protected abstract void assertPositiveResponseBody(String responseBody);

  protected static String writeJson(Object value) {
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  protected static JsonNode parseJson(String body) {
    try {
      return OBJECT_MAPPER.readTree(body);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  protected static <T> T readJson(String body, Class<T> type) {
    try {
      return OBJECT_MAPPER.readValue(body, type);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void ensureSmsMockServerStarted() throws IOException {
    if (smsMockServer == null) {
      smsMockServer = new MockWebServer();
      smsMockServer.start();
    }
  }

  @BeforeAll
  static void startSmsMockServerBeforeAll() throws IOException {
    ensureSmsMockServerStarted();
  }

  @AfterAll
  static void shutdownSmsMockServer() throws IOException {
    if (smsMockServer != null) {
      smsMockServer.shutdown();
      smsMockServer = null;
    }
  }

  private static String smsMockBaseUrl() {
    try {
      ensureSmsMockServerStarted();
      return smsMockServer.url("/").toString().replaceAll("/$", "");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @DynamicPropertySource
  static void registerSecurityMasterBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.security-master.rest.base-url",
        AbstractPortfolioCalculationE2ETest::smsMockBaseUrl);
  }

  @Test
  void shouldReturnServiceUnavailable_whenExternalSecurityMasterIsUnavailable() {
    smsMockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

    var response = postCalculation(requestBodyForSmsUnavailableScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    assertThat(response.responseBody()).contains("SYS-003");
    assertThat(response.responseBody()).contains("Security Master");
  }

  @Test
  void shouldReturnServiceUnavailable_whenExternalSecurityMasterReturnsServerError() {
    smsMockServer.enqueue(new MockResponse()
        .setResponseCode(500)
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody("{\"error\":\"upstream failure\"}"));

    var response = postCalculation(requestBodyForSmsUnavailableScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    assertThat(response.responseBody()).contains("SYS-003");
    assertThat(response.responseBody()).contains("Security Master");
  }

  @Test
  void shouldReturnBadGateway_whenExternalSecurityMasterReturnsClientError() {
    smsMockServer.enqueue(new MockResponse()
        .setResponseCode(400)
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody("{\"error\":\"bad request\"}"));

    var response = postCalculation(requestBodyForSmsUnavailableScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
    assertThat(response.responseBody()).contains("SYS-004");
    assertThat(response.responseBody()).contains("Security Master");
  }

  @Test
  void shouldReturnOk_whenSmsReturnsAvailableResponse() {
    smsMockServer.enqueue(
        new MockResponse()
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(smsPositiveResponseBody()));

    var response = postCalculation(requestBodyForPositiveSmsScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    assertPositiveResponseBody(response.responseBody());
  }

  @Test
  void shouldReturnInternalServerError_whenMetricInBodyDoesNotMatchPathMetric() {
    var response = postCalculation(requestBodyForMismatchedMetricScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.responseBody()).contains("Metric mismatch");
  }

  private HttpResponse postCalculation(String body) {
    return postCalculation(metricPath(), body);
  }

  protected HttpResponse postCalculation(String metric, String body) {
    BodySpec<String, ?> result = webTestClient.post()
        .uri(basePath + "/" + metric)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectBody(String.class);

    var exchangeResult = result.returnResult();
    return new HttpResponse(exchangeResult.getStatus(), exchangeResult.getResponseBody());
  }

  protected record HttpResponse(HttpStatusCode status, String responseBody) {
  }
}
