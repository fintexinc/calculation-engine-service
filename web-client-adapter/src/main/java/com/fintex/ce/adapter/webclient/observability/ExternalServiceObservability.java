package com.fintex.ce.adapter.webclient.observability;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The single way this service observes an outbound call to an external data provider. Every call becomes one
 * observation, which yields both a timer and a span, so provider latency shows up inside the same trace as the request
 * that caused it.
 *
 * <p>
 * <strong>One meter name per concern, the provider carried as the {@code external.service} tag</strong> — every call
 * lands in {@code external.provider.request} regardless of which API was called. A tag can be grouped by *and* split
 * by, so a per-provider view stays available while an aggregate one remains expressible; separate names would only ever
 * allow the split, and would force every dashboard, alert and recording rule to enumerate the providers by hand. This
 * is also what the Micrometer and OpenTelemetry conventions expect, which matters for a metric a downstream backend has
 * to interpret.
 *
 * <p>
 * One entry point: {@link #observe} wraps the blocking call and derives the outcome from whether the action threw.
 * {@code outcome} is {@code success}, {@code http_error} when the provider returned a response the client rejected, or
 * {@code error} when nothing came back at all — a connection failure, a timeout, an unparseable body. That split is
 * driven by whether the client reported a status through {@link #upstreamStatus}, not by the exception type: every
 * client here maps its failures to domain exceptions before {@code observe} can see them, so an {@code instanceof}
 * check against a transport exception would match nothing and quietly file every failure under one outcome.
 *
 * <p>
 * {@code upstream.status} carries the real status when one was reported and {@code none} otherwise. It deliberately
 * does not use the OpenTelemetry {@code http.response.status_code} key: a backend reading that key expects a number on
 * every data point, and a call that never reached the provider has no status to give it.
 *
 * <p>
 * All result tags are seeded when the observation starts so the timer's tag set is identical on every path, and the
 * {@code endpoint} tag has its dynamic path segments collapsed to keep it bounded.
 *
 * <p>
 * This publishes a request timer — carrying count, total time and the percentiles configured under
 * {@code management.metrics.distribution} for the {@code external} prefix — plus a rolling failure ratio and a rolling
 * mean latency, each tagged with the provider it describes.
 */
@Component
public class ExternalServiceObservability {

  public static final String OBSERVATION_NAME = "external.provider.request";
  public static final String RESULT_SIZE_METER_NAME = "external.provider.result.size";
  public static final String FAILURE_RATIO_METER_NAME = "external.provider.request.failure.ratio";
  public static final String MEAN_DURATION_METER_NAME = "external.provider.request.duration.mean";

  static final String UNKNOWN = "unknown";
  static final String NONE = "none";
  static final String SUCCESS = "success";
  static final String HTTP_ERROR = "http_error";
  static final String ERROR = "error";

  static final String SERVICE_TAG = "external.service";
  static final String METHOD_TAG = "http.method";
  static final String ENDPOINT_TAG = "endpoint";
  static final String OUTCOME_TAG = "outcome";
  static final String EXCEPTION_TAG = "exception";
  static final String STATUS_TAG = "upstream.status";
  static final String WINDOW_TAG = "window";

  static final String REQUEST_PATH_KEY = "request.path";
  static final String COMPLETED_EVENT = "external.provider.request.completed";
  static final String FAILED_EVENT = "external.provider.request.failed";

  private static final String ID_PATH_VARIABLE = "{id}";
  private static final String DATE_PATH_VARIABLE = "{date}";
  private static final Pattern UUID_SEGMENT = Pattern.compile(
      "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
  private static final Pattern NUMERIC_SEGMENT = Pattern.compile("^\\d+$");
  private static final Pattern ISO_DATE_SEGMENT = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
  private static final Pattern OPAQUE_ID_SEGMENT = Pattern.compile("(?i)^(?=.*[a-z])(?=.*\\d)[a-z0-9_-]{16,}$");

  private final ObservationRegistry observationRegistry;
  private final MeterRegistry meterRegistry;
  private final Map<String, RollingCallStatistics> statisticsByService = new ConcurrentHashMap<>();

  public ExternalServiceObservability(ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
    this.observationRegistry = observationRegistry;
    this.meterRegistry = meterRegistry;
  }

  /**
   * Observes a blocking provider call whose failures surface as thrown exceptions. The action receives the call handle
   * so that the one place which sees the raw HTTP response — the client's error handler — can report the upstream
   * status through {@link #upstreamStatus} before mapping the response to a domain exception.
   */
  public <R> R observe(String serviceName, String httpMethod, String path, Function<ExternalCall, R> action) {
    ExternalCall call = start(serviceName, httpMethod, path);
    try (Observation.Scope ignored = call.observation().openScope()) {
      R result = action.apply(call);
      completed(call);
      return result;
    } catch (RuntimeException exception) {
      failed(call, exception);
      throw exception;
    } finally {
      stop(call);
    }
  }

  /**
   * Reports the status of an HTTP response the provider actually returned. Clients translate an error response into a
   * domain exception before it reaches {@link #observe}, so the status is knowable only here — without this call the
   * failure could be tagged only as "something threw".
   */
  public void upstreamStatus(ExternalCall call, int status) {
    call.upstreamStatus(status);
  }

  private ExternalCall start(String serviceName, String httpMethod, String path) {
    String serviceTag = valueOrUnknown(serviceName);
    String methodTag = valueOrUnknown(httpMethod);
    String endpointTag = endpointTag(path);
    registerRollingGauges(serviceTag);

    Observation observation = Observation.createNotStarted(OBSERVATION_NAME, observationRegistry)
        .contextualName(serviceTag + " " + methodTag + " " + endpointTag)
        .lowCardinalityKeyValue(SERVICE_TAG, serviceTag)
        .lowCardinalityKeyValue(METHOD_TAG, methodTag)
        .lowCardinalityKeyValue(ENDPOINT_TAG, endpointTag)
        .lowCardinalityKeyValue(OUTCOME_TAG, UNKNOWN)
        .lowCardinalityKeyValue(EXCEPTION_TAG, NONE)
        .lowCardinalityKeyValue(STATUS_TAG, NONE)
        .highCardinalityKeyValue(REQUEST_PATH_KEY, valueOrUnknown(path));

    observation.start();
    return new ExternalCall(observation, serviceTag, System.nanoTime());
  }

  private void completed(ExternalCall call) {
    outcome(call, SUCCESS, NONE, statusTag(call));
    record(call, false);
    call.observation().event(Observation.Event.of(COMPLETED_EVENT));
  }

  /**
   * Reports a failed call, separating a response the provider did return — which carries its status — from a transport
   * or deserialization failure, where there is no status to report and inventing one would be a lie.
   */
  private void failed(ExternalCall call, Throwable cause) {
    outcome(call, call.hasUpstreamStatus() ? HTTP_ERROR : ERROR, simpleName(cause), statusTag(call));
    record(call, true);
    call.observation().error(cause);
    call.observation().event(Observation.Event.of(FAILED_EVENT));
  }

  private void stop(ExternalCall call) {
    call.observation().stop();
  }

  private static String statusTag(ExternalCall call) {
    return call.hasUpstreamStatus() ? String.valueOf(call.upstreamStatus()) : NONE;
  }

  /**
   * Records how many usable items a provider actually returned. Separate from {@link #observe} because a caller only
   * learns the count after mapping the response, by which time the call it belongs to has already been reported — an
   * empty payload is a 200 that satisfied nobody, and only the mapping step can tell.
   */
  public void recordResultSize(String serviceName, String path, int itemCount) {
    DistributionSummary.builder(RESULT_SIZE_METER_NAME)
        .description("Number of usable items returned per call to an external provider")
        .baseUnit("items")
        .tag(SERVICE_TAG, valueOrUnknown(serviceName))
        .tag(ENDPOINT_TAG, endpointTag(path))
        .register(meterRegistry)
        .record(itemCount);
  }

  private void record(ExternalCall call, boolean failure) {
    registerRollingGauges(call.serviceName()).record(System.nanoTime() - call.startNanos(), failure);
  }

  /**
   * Returns this provider's rolling tally, registering its failure-ratio and mean-latency gauges on first use so a
   * provider that is never called publishes no misleading zeroes.
   */
  private RollingCallStatistics registerRollingGauges(String serviceTag) {
    return statisticsByService.computeIfAbsent(serviceTag, service -> {
      RollingCallStatistics statistics = new RollingCallStatistics();
      String window = RollingCallStatistics.WINDOW.toMinutes() + "m";
      Gauge.builder(FAILURE_RATIO_METER_NAME, statistics, RollingCallStatistics::failureRatio)
          .description("Fraction of calls to this provider that failed or returned nothing, over a rolling window")
          .tag(SERVICE_TAG, service)
          .tag(WINDOW_TAG, window)
          .strongReference(true)
          .register(meterRegistry);
      Gauge.builder(MEAN_DURATION_METER_NAME, statistics, RollingCallStatistics::meanDurationMillis)
          .description("Mean duration of calls to this provider over a rolling window")
          .baseUnit("milliseconds")
          .tag(SERVICE_TAG, service)
          .tag(WINDOW_TAG, window)
          .strongReference(true)
          .register(meterRegistry);
      return statistics;
    });
  }

  private static void outcome(ExternalCall call, String outcome, String exception, String status) {
    call.observation()
        .lowCardinalityKeyValue(OUTCOME_TAG, outcome)
        .lowCardinalityKeyValue(EXCEPTION_TAG, exception)
        .lowCardinalityKeyValue(STATUS_TAG, status);
  }

  private static String simpleName(Throwable cause) {
    return cause == null ? NONE : cause.getClass().getSimpleName();
  }

  private static String endpointTag(String path) {
    String value = valueOrUnknown(path);
    int queryStart = value.indexOf('?');
    String pathOnly = queryStart < 0 ? value : value.substring(0, queryStart);
    return normalizePathSegments(pathOnly);
  }

  private static String normalizePathSegments(String path) {
    if (UNKNOWN.equals(path)) {
      return path;
    }
    return Arrays.stream(path.split("/", -1))
        .map(ExternalServiceObservability::normalizePathSegment)
        .reduce((left, right) -> left + "/" + right)
        .orElse(path);
  }

  private static String normalizePathSegment(String segment) {
    if (ISO_DATE_SEGMENT.matcher(segment).matches()) {
      return DATE_PATH_VARIABLE;
    }
    return isDynamicIdSegment(segment) ? ID_PATH_VARIABLE : segment;
  }

  private static boolean isDynamicIdSegment(String segment) {
    return UUID_SEGMENT.matcher(segment).matches()
        || NUMERIC_SEGMENT.matcher(segment).matches()
        || OPAQUE_ID_SEGMENT.matcher(segment).matches();
  }

  private static String valueOrUnknown(String value) {
    return value == null || value.isBlank() ? UNKNOWN : value;
  }

  /**
   * Handle for a started observation, carrying the already normalized service name so outcome reporting cannot disagree
   * with what {@code start} recorded, plus the start timestamp feeding the rolling statistics.
   *
   * <p>
   * The upstream status is the one piece the handle cannot know up front: it is reported mid-call by whoever reads the
   * response. It is {@code volatile} because a reactive client may report it from an event-loop thread while the
   * outcome is written on the thread that blocked.
   */
  public static final class ExternalCall {

    private static final int NO_STATUS = -1;

    private final Observation observation;
    private final String serviceName;
    private final long startNanos;

    private volatile int upstreamStatus = NO_STATUS;

    private ExternalCall(Observation observation, String serviceName, long startNanos) {
      this.observation = observation;
      this.serviceName = serviceName;
      this.startNanos = startNanos;
    }

    Observation observation() {
      return observation;
    }

    String serviceName() {
      return serviceName;
    }

    long startNanos() {
      return startNanos;
    }

    int upstreamStatus() {
      return upstreamStatus;
    }

    boolean hasUpstreamStatus() {
      return upstreamStatus != NO_STATUS;
    }

    void upstreamStatus(int status) {
      this.upstreamStatus = status;
    }
  }
}
