package com.fintex.ce.adapter.webclient.observability;

import org.junit.jupiter.api.Test;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalServiceObservabilityTest {

  @Test
  void shouldReturnProviderResult_whenProviderRequestCompletes() {
    ExternalServiceObservability observability = new ExternalServiceObservability(ObservationRegistry.create());

    String result = observability.observe(
        "bank-of-canada",
        "GET",
        "/observations/FXUSDCAD/json?start_date=2024-01-01&end_date=2024-01-31",
        () -> "ok");

    assertThat(result).isEqualTo("ok");
  }

  @Test
  void shouldPublishTraceContext_whenProviderRequestCompletes() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    ExternalServiceObservability traceObservability = new ExternalServiceObservability(
        observationRegistry(observationHandler));

    String result = traceObservability.observe(
        "bank-of-canada",
        "GET",
        "/observations/FXUSDCAD/json?start_date=2024-01-01&end_date=2024-01-31",
        () -> "ok");

    assertThat(result).isEqualTo("ok");
    assertThat(observationHandler.eventNames).contains(ExternalServiceObservability.COMPLETED_EVENT);
    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(context.getName()).isEqualTo(ExternalServiceObservability.OBSERVATION_NAME);
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.SERVICE_TAG)).isEqualTo(
              "bank-of-canada");
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.METHOD_TAG)).isEqualTo("GET");
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.ENDPOINT_TAG))
              .isEqualTo("/observations/FXUSDCAD/json");
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.OUTCOME_TAG))
              .isEqualTo(ExternalServiceObservability.SUCCESS);
          assertThat(highCardinalityValue(context, ExternalServiceObservability.REQUEST_PATH_KEY))
              .isEqualTo("/observations/FXUSDCAD/json?start_date=2024-01-01&end_date=2024-01-31");
        });
  }

  @Test
  void shouldPublishTraceContext_whenProviderRequestFails() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    ExternalServiceObservability traceObservability = new ExternalServiceObservability(
        observationRegistry(observationHandler));

    assertThatThrownBy(() -> traceObservability.observe(
        "security-master",
        "POST",
        "/api/v1/wealth/securities/fees",
        () -> {
          throw new IllegalArgumentException("invalid");
        }))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid");

    assertThat(observationHandler.eventNames).contains(ExternalServiceObservability.FAILED_EVENT);
    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.SERVICE_TAG)).isEqualTo(
              "security-master");
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.METHOD_TAG)).isEqualTo("POST");
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.ENDPOINT_TAG))
              .isEqualTo("/api/v1/wealth/securities/fees");
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.OUTCOME_TAG))
              .isEqualTo(ExternalServiceObservability.ERROR);
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.EXCEPTION_TAG))
              .isEqualTo(IllegalArgumentException.class.getSimpleName());
        });
  }

  @Test
  void shouldNormalizeEndpointTag_whenProviderPathContainsDynamicSegments() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    ExternalServiceObservability traceObservability = new ExternalServiceObservability(
        observationRegistry(observationHandler));

    traceObservability.observe(
        "security-master",
        "GET",
        "/api/v1/wealth/securities/holdings/12345/details?include=allocations",
        () -> "first");
    traceObservability.observe(
        "security-master",
        "GET",
        "/api/v1/wealth/securities/holdings/67890/details?include=fees",
        () -> "second");

    assertThat(observationHandler.stoppedContexts)
        .hasSize(2)
        .allSatisfy(context -> assertThat(lowCardinalityValue(context, ExternalServiceObservability.ENDPOINT_TAG))
            .isEqualTo("/api/v1/wealth/securities/holdings/{id}/details"));
  }

  private static ObservationRegistry observationRegistry(CapturingObservationHandler observationHandler) {
    ObservationRegistry observationRegistry = ObservationRegistry.create();
    observationRegistry.observationConfig().observationHandler(observationHandler);
    return observationRegistry;
  }

  private static String lowCardinalityValue(Observation.Context context, String key) {
    return context.getLowCardinalityKeyValue(key).getValue();
  }

  private static String highCardinalityValue(Observation.Context context, String key) {
    return context.getHighCardinalityKeyValue(key).getValue();
  }

  private static class CapturingObservationHandler implements ObservationHandler<Observation.Context> {

    private final List<Observation.Context> stoppedContexts = new ArrayList<>();
    private final List<String> eventNames = new ArrayList<>();

    @Override
    public void onStop(Observation.Context context) {
      stoppedContexts.add(context);
    }

    @Override
    public void onEvent(Observation.Event event, Observation.Context context) {
      eventNames.add(event.getName());
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
      return true;
    }
  }
}
