package com.fintex.ce.e2e;

import com.fintex.ce.PortfolioCalculationEngineApplication;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterRestProperties;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;

/**
 * Shared infrastructure for portfolio calculation e2e tests.
 *
 * <p>
 * {@code WebTestClient} uses a longer timeout here so slower CI execution does not cause otherwise valid end-to-end
 * scenarios to fail due to the default client timeout.
 */
@Tag("e2e")
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "60s")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractPortfolioCalculationE2EBase {

  private static final String basePath = "/api/v1/portfolio/calculations";

  /**
   * This module runs e2e tests with {@code reuseForks=false} (see {@code bootstrap/pom.xml}), so each test class gets
   * its own JVM and therefore its own instance of this static server. Classes never share it, and no {@code @AfterAll}
   * shutdown is needed because the server is reclaimed when the per-class JVM exits.
   */
  protected static final MockWebServer smsMockServer = MockWebServers.started();

  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

  @Autowired
  protected WebTestClient webTestClient;

  protected abstract String metricPath();

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

  protected static <T> SecurityAttributeResult<T> securityAttributeResult(
      SecurityIdentifier identifier, T data) {
    return SecurityAttributeResult.<T>builder()
        .identifier(identifier)
        .data(data)
        .build();
  }

  @BeforeEach
  void resetSmsMockServerQueue() {
    smsMockServer.setDispatcher(new QueueDispatcher());
  }

  @DynamicPropertySource
  static void registerSecurityMasterBaseUrl(DynamicPropertyRegistry registry) {
    registry.add(
        SecurityMasterRestProperties.BASE_URL_PROPERTY,
        () -> MockWebServers.baseUrl(smsMockServer));
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