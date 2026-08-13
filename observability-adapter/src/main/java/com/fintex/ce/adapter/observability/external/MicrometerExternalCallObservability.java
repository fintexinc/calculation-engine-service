package com.fintex.ce.adapter.observability.external;

import com.fintex.ce.port.observability.ExternalCallObservability;
import com.fintex.wm.commons.domain.ExternalWebService;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Micrometer implementation of {@link ExternalCallObservability}, publishing provider-level metrics for outbound calls
 * to external data providers.
 *
 * <p>
 * Spans and transport-level timing for these calls come from the framework: every web client is built from the
 * autoconfigured builder, so each call already produces an {@code http.client.requests} timer and a client span
 * correctly parented inside the caller's trace. This class deliberately adds no second span and no second wire timer —
 * it records only what the transport cannot see: whether the response carried usable data and how many items came back.
 *
 * <p>
 * One meter name per measurement, dimensioned by the {@code external.service} and {@code endpoint} tags, so a single
 * dashboard or alert covers every provider and adding a provider needs no new query. Tag values come from
 * {@link ExternalWebService}, {@link ExternalCallOutcome} and the endpoint templates the callers pass, so cardinality
 * is bounded by construction.
 *
 * <p>
 * {@code error.type} follows the OpenTelemetry convention and always carries an exception type, never a status code;
 * {@code upstream.status} carries the status the provider actually returned and {@code none} otherwise. Keeping them
 * apart is what separates a response the provider did return from a call that never reached it, and it deliberately
 * avoids the OpenTelemetry {@code http.response.status_code} key: a backend reading that key expects a number on every
 * data point.
 *
 * <p>
 * The meter names and tag keys are shared with the Security Master Service, which publishes one meter more —
 * {@code external.provider.rate.limiter.wait} — because it is the only one of the two with a client-side rate limiter.
 */
@Component
public class MicrometerExternalCallObservability implements ExternalCallObservability {

  static final String REQUEST_METER = "external.provider.request";
  static final String RESULT_SIZE_METER = "external.provider.result.size";

  static final String SERVICE_TAG = "external.service";
  static final String METHOD_TAG = "http.method";
  static final String ENDPOINT_TAG = "endpoint";
  static final String OUTCOME_TAG = "outcome";
  static final String ERROR_TYPE_TAG = "error.type";
  static final String STATUS_TAG = "upstream.status";

  static final String NONE = "none";
  static final String UNKNOWN = "unknown";

  private final MeterRegistry meterRegistry;
  private final Map<Tags, Timer> requestTimers = new ConcurrentHashMap<>();
  private final Map<Tags, DistributionSummary> resultSizes = new ConcurrentHashMap<>();

  public MicrometerExternalCallObservability(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public ExternalCall start(ExternalWebService service, String httpMethod, String endpoint) {
    return new MicrometerExternalCall(service, valueOrUnknown(httpMethod), endpointTag(endpoint));
  }

  private Timer requestTimer(Tags tags) {
    return requestTimers.computeIfAbsent(tags, timerTags -> Timer.builder(REQUEST_METER)
        .description("Duration and outcome of calls to external data providers")
        .tags(timerTags)
        .register(meterRegistry));
  }

  private DistributionSummary resultSize(Tags tags) {
    return resultSizes.computeIfAbsent(tags, summaryTags -> DistributionSummary.builder(RESULT_SIZE_METER)
        .description("Number of usable items returned per call to an external data provider")
        .baseUnit("items")
        .tags(summaryTags)
        .register(meterRegistry));
  }

  private static String errorType(Throwable cause) {
    return cause == null ? NONE : cause.getClass().getSimpleName();
  }

  /**
   * Drops the query string, so that one endpoint called with different parameters stays one series.
   */
  private static String endpointTag(String endpoint) {
    String value = valueOrUnknown(endpoint);
    int queryStart = value.indexOf('?');
    return queryStart < 0 ? value : valueOrUnknown(value.substring(0, queryStart));
  }

  private static String valueOrUnknown(String value) {
    return value == null || value.isBlank() ? UNKNOWN : value;
  }

  /**
   * Handle for a call in flight, carrying the tag values settled at {@link #start} so an outcome can never be filed
   * against a different provider, method or endpoint than the one measured.
   */
  private final class MicrometerExternalCall implements ExternalCall {

    private final Tags callTags;
    private final Tags endpointTags;
    private final long startNanos = System.nanoTime();
    private final AtomicBoolean outcomeReported = new AtomicBoolean();

    private MicrometerExternalCall(ExternalWebService service, String httpMethod, String endpoint) {
      this.endpointTags = Tags.of(SERVICE_TAG, service.id(), ENDPOINT_TAG, endpoint);
      this.callTags = endpointTags.and(METHOD_TAG, httpMethod);
    }

    @Override
    public void completed(int itemCount) {
      int items = Math.max(0, itemCount);
      resultSize(endpointTags).record(items);
      record(items == 0 ? ExternalCallOutcome.EMPTY : ExternalCallOutcome.SUCCESS, NONE, NONE);
    }

    @Override
    public void failed(Throwable cause) {
      record(ExternalCallOutcome.ERROR, errorType(cause), NONE);
    }

    @Override
    public void httpFailed(int statusCode, Throwable cause) {
      record(ExternalCallOutcome.HTTP_ERROR, errorType(cause), String.valueOf(statusCode));
    }

    private void record(ExternalCallOutcome outcome, String errorType, String upstreamStatus) {
      if (!outcomeReported.compareAndSet(false, true)) {
        return;
      }
      Tags tags = callTags
          .and(OUTCOME_TAG, outcome.id())
          .and(ERROR_TYPE_TAG, errorType)
          .and(STATUS_TAG, upstreamStatus);
      requestTimer(tags).record(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
    }
  }
}
