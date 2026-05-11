package com.fintex.ce.actuator;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterRestProperties;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.RestClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

class SmsHealthIndicatorTest {

  private MockWebServer server;
  private SmsHealthIndicator indicator;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();

    SecurityMasterRestProperties properties = new SecurityMasterRestProperties();
    properties.setBaseUrl(server.url("/").toString());
    properties.setHealthCheckPath("/actuator/health");
    indicator = new SmsHealthIndicator(properties, RestClient.builder());
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void shouldReportUp_whenSmsReturns200() {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("{\"status\":\"UP\"}"));

    Health health = indicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsEntry("status", 200);
  }

  @Test
  void shouldReportDown_whenSmsReturns503() {
    server.enqueue(new MockResponse()
        .setResponseCode(503)
        .setHeader("Content-Type", "application/json")
        .setBody("{\"status\":\"DOWN\"}"));

    Health health = indicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("status", 503);
  }

  @Test
  void shouldReportDown_whenSmsIsUnreachable() throws IOException {
    server.shutdown();

    Health health = indicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsKey("exception");
  }

  @Test
  void shouldReportDownWithinTimeout_whenSmsDoesNotRespond() {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    Duration ceiling = HealthCheckRestClientFactory.HEALTH_CHECK_TIMEOUT.plusSeconds(2);

    long start = System.nanoTime();
    Health health = indicator.health();
    Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(elapsed).isLessThan(ceiling);
  }
}