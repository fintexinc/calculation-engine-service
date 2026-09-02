package ca.tangerine.pce.actuator;

import ca.tangerine.pce.webclient.boc.client.BankOfCanadaProperties;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
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

class BankOfCanadaHealthIndicatorTest {

  private MockWebServer server;
  private BankOfCanadaHealthIndicator indicator;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();

    BankOfCanadaProperties properties = new BankOfCanadaProperties();
    properties.setBaseUrl(server.url("/").toString());
    properties.setHealthCheckPath("/lists/series/json");
    indicator = new BankOfCanadaHealthIndicator(properties, RestClient.builder());
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void shouldReportUp_whenBocReturns200() {
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("{}"));

    Health health = indicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsEntry("status", 200);
  }

  @Test
  void shouldReportDown_whenBocReturnsErrorStatus() {
    server.enqueue(new MockResponse()
        .setResponseCode(500)
        .setHeader("Content-Type", "application/json")
        .setBody("{\"error\":\"upstream\"}"));

    Health health = indicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("status", 500);
  }

  @Test
  void shouldReportDown_whenBocIsUnreachable() throws IOException {
    server.shutdown();

    Health health = indicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsKey("exception");
  }

  @Test
  void shouldReportDownWithinTimeout_whenBocDoesNotRespond() {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    Duration ceiling = HealthCheckRestClientFactory.HEALTH_CHECK_TIMEOUT.plusSeconds(2);

    long start = System.nanoTime();
    Health health = indicator.health();
    Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(elapsed).isLessThan(ceiling);
  }
}