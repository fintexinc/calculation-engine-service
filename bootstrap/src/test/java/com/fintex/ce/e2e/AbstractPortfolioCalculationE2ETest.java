package com.fintex.ce.e2e;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.SocketPolicy;

/**
 * Shared scenarios every calculation endpoint must satisfy: the upstream being unavailable, answering with a server or
 * a client error, answering correctly, and the request contradicting the path it was sent to.
 */
abstract class AbstractPortfolioCalculationE2ETest extends AbstractPortfolioCalculationE2EBase {
  /**
   * Mirrors {@code resilience4j.retry.configs.default.max-attempts}: a transient SMS failure is attempted this many
   * times before the engine gives up, so tests that exhaust the retries must enqueue this many failing responses.
   */
  private static final int SMS_RETRY_MAX_ATTEMPTS = 3;

  protected abstract String requestBodyForSmsUnavailableScenario();

  protected abstract String requestBodyForPositiveSmsScenario();

  protected abstract String smsPositiveResponseBody();

  protected abstract String requestBodyForMismatchedMetricScenario();

  protected abstract void assertPositiveResponseBody(String responseBody);

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
}
