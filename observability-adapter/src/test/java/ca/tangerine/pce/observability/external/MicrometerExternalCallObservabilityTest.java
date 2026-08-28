package ca.tangerine.pce.observability.external;

import ca.tangerine.pce.port.observability.ExternalCallObservability.ExternalCall;
import ca.tangerine.wm.commons.domain.ExternalWebService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerExternalCallObservabilityTest {

  private static final String BOC_ENDPOINT = "/observations/FXUSDCAD/json";
  private static final String MIC_ENDPOINT = "/api/v1/wealth/securities";
  private static final String GET = "GET";

  private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
  private final MicrometerExternalCallObservability observability = new MicrometerExternalCallObservability(
      meterRegistry);

  @Test
  void shouldReportSuccessWithNoError_whenProviderReturnedItems() {
    observability.start(ExternalWebService.BANK_OF_CANADA, GET, BOC_ENDPOINT).completed(22);

    Timer timer = requestTimer(ExternalCallOutcome.SUCCESS, ExternalWebService.BANK_OF_CANADA, BOC_ENDPOINT);
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);
    assertThat(timer.getId().getTag(MicrometerExternalCallObservability.METHOD_TAG)).isEqualTo(GET);
    assertThat(timer.getId().getTag(MicrometerExternalCallObservability.ERROR_TYPE_TAG))
        .isEqualTo(MicrometerExternalCallObservability.NONE);
    assertThat(timer.getId().getTag(MicrometerExternalCallObservability.STATUS_TAG))
        .as("a call that succeeded has no upstream error status to report")
        .isEqualTo(MicrometerExternalCallObservability.NONE);
  }

  /**
   * A payload carrying nothing is a 200 that satisfied nobody, so it must not be able to hide inside the success count.
   */
  @Test
  void shouldReportEmptyApartFromSuccess_whenProviderReturnedNoItems() {
    observability.start(ExternalWebService.MARKET_INVESTMENT_CATALOGUE, GET, MIC_ENDPOINT).completed(0);

    assertThat(requestTimer(ExternalCallOutcome.EMPTY, ExternalWebService.MARKET_INVESTMENT_CATALOGUE, MIC_ENDPOINT))
        .isNotNull();
    assertThat(requestTimer(ExternalCallOutcome.SUCCESS, ExternalWebService.MARKET_INVESTMENT_CATALOGUE, MIC_ENDPOINT))
        .as("an empty payload must not be able to hide inside the success count")
        .isNull();
  }

  @Test
  void shouldRecordResultSizePerServiceAndEndpoint_whenProvidersReturnItems() {
    observability.start(ExternalWebService.BANK_OF_CANADA, GET, BOC_ENDPOINT + "?start_date=2024-01-01").completed(22);
    observability.start(ExternalWebService.BANK_OF_CANADA, GET, BOC_ENDPOINT + "?start_date=2024-02-01").completed(0);
    observability.start(ExternalWebService.MARKET_INVESTMENT_CATALOGUE, GET, MIC_ENDPOINT).completed(7);

    DistributionSummary bocSizes = resultSize(ExternalWebService.BANK_OF_CANADA, BOC_ENDPOINT);
    assertThat(bocSizes).isNotNull();
    assertThat(bocSizes.count())
        .as("the query string must not split one endpoint into two meters")
        .isEqualTo(2);
    assertThat(bocSizes.totalAmount()).isEqualTo(22.0);

    DistributionSummary micSizes = resultSize(ExternalWebService.MARKET_INVESTMENT_CATALOGUE, MIC_ENDPOINT);
    assertThat(micSizes).isNotNull();
    assertThat(micSizes.totalAmount()).isEqualTo(7.0);
  }

  @Test
  void shouldReportErrorWithoutAStatus_whenNothingCameBackFromTheProvider() {
    observability.start(ExternalWebService.MARKET_INVESTMENT_CATALOGUE, GET, MIC_ENDPOINT)
        .failed(new IllegalStateException("connection reset"));

    Timer timer = requestTimer(ExternalCallOutcome.ERROR, ExternalWebService.MARKET_INVESTMENT_CATALOGUE, MIC_ENDPOINT);
    assertThat(timer).isNotNull();
    assertThat(timer.getId().getTag(MicrometerExternalCallObservability.ERROR_TYPE_TAG))
        .isEqualTo(IllegalStateException.class.getSimpleName());
    assertThat(timer.getId().getTag(MicrometerExternalCallObservability.STATUS_TAG))
        .as("a call that never reached the provider has no status to report")
        .isEqualTo(MicrometerExternalCallObservability.NONE);
  }

  /**
   * The clients translate an error response into a domain exception before the outcome is filed, so the status has to
   * be carried separately from the exception type rather than derived from it.
   */
  @ParameterizedTest
  @ValueSource(ints = {400, 404, 429, 500, 503})
  void shouldReportHttpErrorWithTheRealStatus_whenProviderReturnedAnErrorResponse(int status) {
    observability.start(ExternalWebService.MARKET_INVESTMENT_CATALOGUE, GET, MIC_ENDPOINT)
        .httpFailed(status, new IllegalArgumentException("mapped to a domain exception by the client"));

    Timer timer = requestTimer(ExternalCallOutcome.HTTP_ERROR, ExternalWebService.MARKET_INVESTMENT_CATALOGUE,
        MIC_ENDPOINT);
    assertThat(timer).isNotNull();
    assertThat(timer.getId().getTag(MicrometerExternalCallObservability.STATUS_TAG))
        .as("the status must be the one the provider actually returned, not a stand-in")
        .isEqualTo(String.valueOf(status));
    assertThat(timer.getId().getTag(MicrometerExternalCallObservability.ERROR_TYPE_TAG))
        .as("error.type always carries an exception type, never a status code")
        .isEqualTo(IllegalArgumentException.class.getSimpleName());
  }

  /**
   * The clients report the status from the error handler and then let the domain exception they mapped it to propagate,
   * so one failed call reaches the handle twice. Without the guard it would be counted as both an http_error and an
   * error.
   */
  @Test
  void shouldKeepTheFirstOutcome_whenTheMappedExceptionFollowsAReportedStatus() {
    ExternalCall call = observability.start(ExternalWebService.MARKET_INVESTMENT_CATALOGUE, GET, MIC_ENDPOINT);
    call.httpFailed(503, new IllegalStateException("mapped by the client"));
    call.failed(new IllegalStateException("the same failure, propagating"));

    assertThat(requestTimer(ExternalCallOutcome.HTTP_ERROR, ExternalWebService.MARKET_INVESTMENT_CATALOGUE,
        MIC_ENDPOINT).count())
        .isEqualTo(1);
    assertThat(requestTimer(ExternalCallOutcome.ERROR, ExternalWebService.MARKET_INVESTMENT_CATALOGUE, MIC_ENDPOINT))
        .as("a response the provider did return must not also be counted as one that never arrived")
        .isNull();
    assertThat(meterRegistry.find(MicrometerExternalCallObservability.REQUEST_METER).timers())
        .as("one call must contribute exactly one sample")
        .hasSize(1);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void shouldTagMethodAndEndpointAsUnknown_whenTheyAreMissing(String missing) {
    observability.start(ExternalWebService.MARKET_INVESTMENT_CATALOGUE, missing, missing).completed(1);

    Timer timer = meterRegistry.find(MicrometerExternalCallObservability.REQUEST_METER)
        .tag(MicrometerExternalCallObservability.METHOD_TAG, MicrometerExternalCallObservability.UNKNOWN)
        .tag(MicrometerExternalCallObservability.ENDPOINT_TAG, MicrometerExternalCallObservability.UNKNOWN)
        .timer();
    assertThat(timer).isNotNull();
  }

  @Test
  void shouldPublishNoMeters_whenNoCallWasEverMade() {
    assertThat(meterRegistry.getMeters())
        .as("a provider that is never called must not publish misleading zeroes")
        .isEmpty();
  }

  private Timer requestTimer(ExternalCallOutcome outcome, ExternalWebService service, String endpoint) {
    return meterRegistry.find(MicrometerExternalCallObservability.REQUEST_METER)
        .tag(MicrometerExternalCallObservability.SERVICE_TAG, service.id())
        .tag(MicrometerExternalCallObservability.ENDPOINT_TAG, endpoint)
        .tag(MicrometerExternalCallObservability.OUTCOME_TAG, outcome.id())
        .timer();
  }

  private DistributionSummary resultSize(ExternalWebService service, String endpoint) {
    return meterRegistry.find(MicrometerExternalCallObservability.RESULT_SIZE_METER)
        .tag(MicrometerExternalCallObservability.SERVICE_TAG, service.id())
        .tag(MicrometerExternalCallObservability.ENDPOINT_TAG, endpoint)
        .summary();
  }
}
