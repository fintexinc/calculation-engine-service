package com.fintex.ce.adapter.webclient.observability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
    ExternalServiceObservability observability = new ExternalServiceObservability(
        ObservationRegistry.create(), new SimpleMeterRegistry());

    String result = observability.observe(
        "bank-of-canada",
        "GET",
        "/observations/FXUSDCAD/json?start_date=2024-01-01&end_date=2024-01-31",
        call -> "ok");

    assertThat(result).isEqualTo("ok");
  }

  @Test
  void shouldPublishTraceContext_whenProviderRequestCompletes() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    ExternalServiceObservability traceObservability = new ExternalServiceObservability(
        observationRegistry(observationHandler), new SimpleMeterRegistry());

    String result = traceObservability.observe(
        "bank-of-canada",
        "GET",
        "/observations/FXUSDCAD/json?start_date=2024-01-01&end_date=2024-01-31",
        call -> "ok");

    assertThat(result).isEqualTo("ok");
    assertThat(observationHandler.eventNames).contains(ExternalServiceObservability.COMPLETED_EVENT);
    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(context.getName())
              .as("every provider shares one timer name, told apart by the external.service tag")
              .isEqualTo(ExternalServiceObservability.OBSERVATION_NAME);
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
        observationRegistry(observationHandler), new SimpleMeterRegistry());

    assertThatThrownBy(() -> traceObservability.observe(
        "security-master",
        "POST",
        "/api/v1/wealth/securities/fees",
        call -> {
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
              .as("nothing came back from the provider, so this is not an http_error")
              .isEqualTo(ExternalServiceObservability.ERROR);
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.EXCEPTION_TAG))
              .isEqualTo(IllegalArgumentException.class.getSimpleName());
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.STATUS_TAG))
              .as("a call that never reached the provider has no status to report")
              .isEqualTo(ExternalServiceObservability.NONE);
        });
  }

  /**
   * The clients translate an error response into a domain exception before {@code observe} can see it, so the outcome
   * has to follow the status the client reported rather than the type of whatever was finally thrown.
   */
  @ParameterizedTest
  @ValueSource(ints = {400, 404, 429, 500, 503})
  void shouldReportHttpErrorWithTheRealStatus_whenProviderReturnedAnErrorResponse(int status) {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    ExternalServiceObservability observability = new ExternalServiceObservability(
        observationRegistry(observationHandler), new SimpleMeterRegistry());

    assertThatThrownBy(() -> observability.observe("security-master", "GET", "/api/v1/wealth/securities", call -> {
      observability.upstreamStatus(call, status);
      throw new IllegalStateException("mapped to a domain exception by the client");
    })).isInstanceOf(IllegalStateException.class);

    assertThat(observationHandler.stoppedContexts)
        .singleElement()
        .satisfies(context -> {
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.OUTCOME_TAG))
              .isEqualTo(ExternalServiceObservability.HTTP_ERROR);
          assertThat(lowCardinalityValue(context, ExternalServiceObservability.STATUS_TAG))
              .as("the status must be the one the provider actually returned, not a stand-in")
              .isEqualTo(String.valueOf(status));
        });
  }

  @Test
  void shouldPublishFailureRatioAndMeanDurationGauges_whenCallsSucceedAndFail() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    ExternalServiceObservability observability = new ExternalServiceObservability(
        ObservationRegistry.create(), meterRegistry);

    observability.observe("security-master", "GET", "/api/v1/wealth/securities", call -> "ok");
    observability.observe("security-master", "GET", "/api/v1/wealth/securities", call -> "ok");
    observability.observe("security-master", "GET", "/api/v1/wealth/securities", call -> "ok");
    assertThatThrownBy(() -> observability.observe("security-master", "GET", "/api/v1/wealth/securities", call -> {
      throw new IllegalStateException("boom");
    })).isInstanceOf(IllegalStateException.class);

    assertThat(gauge(meterRegistry, ExternalServiceObservability.FAILURE_RATIO_METER_NAME, "security-master"))
        .isNotNull()
        .satisfies(failureRatio -> assertThat(failureRatio.value()).isEqualTo(0.25));
    assertThat(gauge(meterRegistry, ExternalServiceObservability.MEAN_DURATION_METER_NAME, "security-master"))
        .isNotNull()
        .satisfies(meanDuration -> assertThat(meanDuration.value()).isNotNegative());
    assertThat(meterRegistry.find(ExternalServiceObservability.FAILURE_RATIO_METER_NAME).gauges())
        .as("a provider that was never called must not publish a misleading zero")
        .hasSize(1);
    assertThat(meterRegistry.find(ExternalServiceObservability.FAILURE_RATIO_METER_NAME).gauge().getId()
        .getTag(ExternalServiceObservability.WINDOW_TAG))
        .as("a ratio without its window is unreadable")
        .isEqualTo(RollingCallStatistics.WINDOW.toMinutes() + "m");

    Timer requestTimer = meterRegistry.find(ExternalServiceObservability.OBSERVATION_NAME).timer();
    assertThat(requestTimer)
        .as("the observation registry has no meter handler here, so only the explicit meters are published")
        .isNull();
  }

  @Test
  void shouldRecordResultSizePerServiceAndEndpoint_whenProvidersReturnItems() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    ExternalServiceObservability observability = new ExternalServiceObservability(
        ObservationRegistry.create(), meterRegistry);

    observability.recordResultSize("bank-of-canada", "/observations/FXUSDCAD/json?start_date=2024-01-01", 22);
    observability.recordResultSize("bank-of-canada", "/observations/FXUSDCAD/json?start_date=2024-02-01", 0);
    observability.recordResultSize("security-master", "/api/v1/wealth/securities/12345", 7);

    DistributionSummary bocSizes = resultSize(meterRegistry, "bank-of-canada", "/observations/FXUSDCAD/json");
    assertThat(bocSizes).isNotNull();
    assertThat(bocSizes.count())
        .as("the query string must not split one endpoint into two meters")
        .isEqualTo(2);
    assertThat(bocSizes.totalAmount()).isEqualTo(22.0);
    assertThat(bocSizes.max()).isEqualTo(22.0);

    DistributionSummary smSizes = resultSize(meterRegistry, "security-master", "/api/v1/wealth/securities/{id}");
    assertThat(smSizes).isNotNull();
    assertThat(smSizes.count()).isEqualTo(1);
    assertThat(smSizes.totalAmount()).isEqualTo(7.0);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void shouldTagServiceAsUnknown_whenServiceNameIsMissing(String serviceName) {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    ExternalServiceObservability observability = new ExternalServiceObservability(
        ObservationRegistry.create(), meterRegistry);

    observability.observe(serviceName, null, null, call -> "ok");

    assertThat(gauge(meterRegistry, ExternalServiceObservability.FAILURE_RATIO_METER_NAME,
        ExternalServiceObservability.UNKNOWN)).isNotNull();
  }

  private static Gauge gauge(SimpleMeterRegistry meterRegistry, String name, String serviceName) {
    return meterRegistry.find(name).tag(ExternalServiceObservability.SERVICE_TAG, serviceName).gauge();
  }

  private static DistributionSummary resultSize(SimpleMeterRegistry meterRegistry, String service, String endpoint) {
    return meterRegistry.find(ExternalServiceObservability.RESULT_SIZE_METER_NAME)
        .tag(ExternalServiceObservability.SERVICE_TAG, service)
        .tag(ExternalServiceObservability.ENDPOINT_TAG, endpoint)
        .summary();
  }

  @Test
  void shouldNormalizeEndpointTag_whenProviderPathContainsDynamicSegments() {
    CapturingObservationHandler observationHandler = new CapturingObservationHandler();
    ExternalServiceObservability traceObservability = new ExternalServiceObservability(
        observationRegistry(observationHandler), new SimpleMeterRegistry());

    traceObservability.observe(
        "security-master",
        "GET",
        "/api/v1/wealth/securities/holdings/12345/details?include=allocations",
        call -> "first");
    traceObservability.observe(
        "security-master",
        "GET",
        "/api/v1/wealth/securities/holdings/67890/details?include=fees",
        call -> "second");

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
