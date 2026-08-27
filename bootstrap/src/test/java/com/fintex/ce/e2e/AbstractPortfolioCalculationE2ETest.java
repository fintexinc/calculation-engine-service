package com.fintex.ce.e2e;

import com.fintex.ce.PortfolioCalculationEngineApplication;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterRestProperties;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.SocketPolicy;

/**
 * Shared scenarios every calculation endpoint must satisfy: the upstream being unavailable, answering with a server or
 * a client error, answering correctly, and the request contradicting the path it was sent to.
 *
 * <p>
 * {@code WebTestClient} defaults to a five-second response timeout, which is a statement about how fast a healthy
 * service replies, not about how fast a CI agent running several application contexts at once gets around to replying.
 * The timeout is raised so a slow agent produces a slow test rather than a failed one; the assertions, not the clock,
 * decide whether the behaviour is correct.
 */
@Tag("e2e")
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "60s")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractPortfolioCalculationE2ETest {

  private static final String basePath = "/api/v1/portfolio/calculations";

  /**
   * Mirrors {@code resilience4j.retry.configs.default.max-attempts}: a transient SMS failure is attempted this many
   * times before the engine gives up, so tests that exhaust the retries must enqueue this many failing responses.
   */
  private static final int SMS_RETRY_MAX_ATTEMPTS = 3;

  /**
   * This module runs e2e tests with {@code reuseForks=false} (see {@code bootstrap/pom.xml}), so each test class gets
   * its own JVM and therefore its own instance of this static server — classes never share it. That isolation is what
   * prevents mock state from leaking or racing across classes. No {@code @AfterAll} shutdown is needed: the server is
   * reclaimed when the per-class JVM exits.
   */
  protected static final MockWebServer smsMockServer = MockWebServers.started();

  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

  @Autowired
  protected WebTestClient webTestClient;

  protected abstract String metricPath();

  protected abstract String requestBodyForSmsUnavailableScenario();

  protected abstract String requestBodyForPositiveSmsScenario();

  protected abstract String smsPositiveResponseBody();

  protected abstract String requestBodyForMismatchedMetricScenario();

  protected abstract void assertPositiveResponseBody(String responseBody);

  protected record HttpResponse(HttpStatusCode status, String responseBody) {
  }

  protected void enqueueSmsMockResponse(String body) {
    smsMockServer.enqueue(
        new MockResponse()
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(body));
  }

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

  protected static <T> SecurityAttributeResult<T> securityAttributeResult(SecurityIdentifier identifier, T data) {
    return SecurityAttributeResult.<T>builder()
        .identifier(identifier)
        .data(data)
        .build();
  }

  /**
   * Reset this class's {@link #smsMockServer} queue before every test so a response enqueued by one test can never leak
   * into the next test in the same class (the server is shared by all tests within a class). Cross-class isolation is
   * handled separately by {@code reuseForks=false}, which gives each test class its own JVM and thus its own server.
   */
  @BeforeEach
  void resetSmsMockServerQueue() {
    smsMockServer.setDispatcher(new QueueDispatcher());
  }

  @DynamicPropertySource
  static void registerSecurityMasterBaseUrl(DynamicPropertyRegistry registry) {
    registry.add(SecurityMasterRestProperties.BASE_URL_PROPERTY, () -> MockWebServers.baseUrl(smsMockServer));
  }

  @Test
  void shouldReturnServiceUnavailableAfterRetries_whenExternalSecurityMasterIsUnavailable() {
    for (int attempt = 0; attempt < SMS_RETRY_MAX_ATTEMPTS; attempt++) {
      smsMockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
    }
    int requestsBefore = smsMockServer.getRequestCount();

    var response = postCalculation(requestBodyForSmsUnavailableScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    assertThat(response.responseBody()).contains(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getCode());
    assertThat(response.responseBody()).contains("Security Master");
    assertThat(smsMockServer.getRequestCount() - requestsBefore)
        .as("a transport failure is transient, so every configured attempt must reach the server")
        .isEqualTo(SMS_RETRY_MAX_ATTEMPTS);
  }

  @Test
  void shouldReturnServiceUnavailableAfterRetries_whenExternalSecurityMasterReturnsServerError() {
    for (int attempt = 0; attempt < SMS_RETRY_MAX_ATTEMPTS; attempt++) {
      smsMockServer.enqueue(new MockResponse()
          .setResponseCode(500)
          .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .setBody("{\"error\":\"upstream failure\"}"));
    }
    int requestsBefore = smsMockServer.getRequestCount();

    var response = postCalculation(requestBodyForSmsUnavailableScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    assertThat(response.responseBody()).contains(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getCode());
    assertThat(response.responseBody()).contains("Security Master");
    assertThat(smsMockServer.getRequestCount() - requestsBefore)
        .as("a server error is transient, so every configured attempt must reach the server")
        .isEqualTo(SMS_RETRY_MAX_ATTEMPTS);
  }

  @Test
  void shouldReturnBadGatewayWithoutRetrying_whenExternalSecurityMasterReturnsClientError() {
    smsMockServer.enqueue(new MockResponse()
        .setResponseCode(400)
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody("{\"error\":\"bad request\"}"));
    int requestsBefore = smsMockServer.getRequestCount();

    var response = postCalculation(requestBodyForSmsUnavailableScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
    assertThat(response.responseBody()).contains(ErrorCode.EXTERNAL_SERVICE_BAD_RESPONSE.getCode());
    assertThat(response.responseBody()).contains("Security Master");
    assertThat(smsMockServer.getRequestCount() - requestsBefore)
        .as("a rejected request is this service's own bug, so repeating it cannot help")
        .isEqualTo(1);
  }

  /**
   * Subclasses that trigger extra Security Master calls (e.g. treasury rates for trailing returns) override this to
   * enqueue all required mock responses in order.
   */
  protected void enqueueForPositiveSmsScenario() {
    enqueueSmsMockResponse(smsPositiveResponseBody());
  }

  @Test
  void shouldReturnOk_whenSmsReturnsAvailableResponse() {
    enqueueForPositiveSmsScenario();

    var response = postCalculation(requestBodyForPositiveSmsScenario());

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    assertPositiveResponseBody(response.responseBody());
  }

  @Test
  void shouldReturnBadRequest_whenMetricInBodyDoesNotMatchPathMetric() {
    String requestBody = requestBodyForMismatchedMetricScenario();
    String bodyMetric = parseJson(requestBody).path("metric").asText();

    var response = postCalculation(requestBody);

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.METRIC_MISMATCH.getCode());
    assertThat(notification.getMessage())
        .isEqualTo(ErrorCode.METRIC_MISMATCH.getFormattedMessage(metricPath(), bodyMetric));
    assertThat(notification.getDescription()).isEqualTo(ErrorCode.METRIC_MISMATCH.getDescription());
    assertThat(notification.getAction()).isEqualTo(ErrorCode.METRIC_MISMATCH.getAction());
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
    assertThat(notification.getMetadata())
        .containsOnlyKeys("param-1", "param-2")
        .containsEntry("param-1", metricPath())
        .containsEntry("param-2", bodyMetric);
  }

  protected HttpResponse postCalculation(String body) {
    BodySpec<String, ?> result = webTestClient.post()
        .uri(basePath + "/" + metricPath())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectBody(String.class);

    var exchangeResult = result.returnResult();
    return new HttpResponse(exchangeResult.getStatus(), exchangeResult.getResponseBody());
  }
}
