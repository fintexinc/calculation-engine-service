package ca.tangerine.pce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.PortfolioCalculationEngineApplication;
import ca.tangerine.pce.webclient.resilience.ExternalCallResilience;
import reactor.core.publisher.Mono;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RetryTracePropagationIntegrationTest {

  private static final int MAX_ATTEMPTS = 3;
  private static final String RESPONSE = "response";

  @Autowired
  @Qualifier("marketInvestmentCatalogueCallResilience")
  private ExternalCallResilience resilience;

  @Autowired
  private ObservationRegistry observationRegistry;

  @Autowired
  private Tracer tracer;

  @Test
  void shouldKeepTraceId_whenSecondAndThirdAttemptsAreRetried() {
    Observation parentObservation = Observation.start("retry trace propagation", observationRegistry);
    AtomicInteger attempts = new AtomicInteger();
    List<String> traceIds = new ArrayList<>();
    String parentTraceId;
    String response;
    try (Observation.Scope ignored = parentObservation.openScope()) {
      parentTraceId = tracer.currentSpan().context().traceId();
      response = resilience.decorate(Mono.defer(() -> {
        Span currentSpan = tracer.currentSpan();
        traceIds.add(currentSpan == null ? null : currentSpan.context().traceId());
        return attempts.incrementAndGet() < MAX_ATTEMPTS
            ? Mono.error(new IOException("transient failure"))
            : Mono.just(RESPONSE);
      })).block();
    } finally {
      parentObservation.stop();
    }

    assertThat(response).isEqualTo(RESPONSE);
    assertThat(attempts).hasValue(MAX_ATTEMPTS);
    assertThat(traceIds).containsExactly(parentTraceId, parentTraceId, parentTraceId);
  }
}
