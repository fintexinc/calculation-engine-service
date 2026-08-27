package ca.tangerine.pce.observability.calculation;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.result.composite.CompositeCalculationResult;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.BasePceException;
import ca.tangerine.pce.model.error.exceptions.CalculationsFailedException;
import ca.tangerine.pce.port.observability.CalculationDurationRecorder;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

/**
 * Statistics for every {@link CalculationMetric} the service calculates, always keyed by the metric that actually ran.
 * A composite request is decomposed into its member commands and contributes one execution record per member metric, so
 * {@code composite} never appears as a value of the {@code calculation.metric} tag and the endpoint a client happened
 * to use leaves no trace in these meters.
 *
 * <p>
 * Recorded per metric: execution counts split by outcome (the failure ratio is derived from them), how long the
 * calculation itself took, the distribution of warning counts across successful executions plus the smallest count
 * seen, and the frequency of each error and warning code. Durations arrive through {@link CalculationDurationRecorder},
 * which the orchestrator calls around each individual metric, so a member of a composite request is timed separately
 * from its siblings. Error codes come from {@link ErrorCode} when the failure carries one and fall back to the
 * exception's simple name otherwise, which keeps the tag bounded either way.
 *
 * <p>
 * Which percentiles the duration timer publishes is configuration, not code: it comes from
 * {@code management.metrics.distribution.percentiles} for the {@code portfolio.calculation} prefix, so the registry is
 * the single place that decides. A registry without that configuration still records counts, mean and max.
 */
@Component
public class CalculationMetricStatistics implements CalculationDurationRecorder {

  public static final String EXECUTIONS_METER_NAME = "portfolio.calculation.executions";
  public static final String DURATION_METER_NAME = "portfolio.calculation.duration";
  public static final String WARNINGS_METER_NAME = "portfolio.calculation.warnings";
  public static final String WARNINGS_MIN_METER_NAME = "portfolio.calculation.warnings.min";
  public static final String ERROR_CODES_METER_NAME = "portfolio.calculation.errors";
  public static final String WARNING_CODES_METER_NAME = "portfolio.calculation.warning.codes";
  public static final String HOLDINGS_METER_NAME = "portfolio.calculation.holdings";
  public static final String BENCHMARK_HOLDINGS_METER_NAME = "portfolio.calculation.benchmark.holdings";

  public static final String METRIC_TAG = "calculation.metric";
  public static final String OUTCOME_TAG = "outcome";
  public static final String ERROR_CODE_TAG = "error.code";
  public static final String WARNING_CODE_TAG = "warning.code";

  public static final String SUCCESS = "success";
  public static final String ERROR = "error";

  static final String UNKNOWN = "unknown";
  static final String UNMAPPED = "unmapped";

  private final MeterRegistry meterRegistry;
  private final ConcurrentMap<String, AtomicLong> minWarningsByMetric = new ConcurrentHashMap<>();

  public CalculationMetricStatistics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public void recordSuccess(CalculationMetric metric, Duration duration) {
    recordDuration(metricTag(metric), SUCCESS, duration);
  }

  @Override
  public void recordFailure(CalculationMetric metric, Duration duration) {
    recordDuration(metricTag(metric), ERROR, duration);
  }

  public void recordSingleSuccess(String metricTag, BaseCalculationResult result, RequestShape shape) {
    recordMetricSuccess(metricTag, warningsOf(result), shape);
  }

  public void recordSingleFailure(String metricTag, Throwable cause, RequestShape shape) {
    countExecution(metricTag, ERROR);
    recordShape(metricTag, shape);
    errorCodesOf(cause).forEach(code -> countCode(ERROR_CODES_METER_NAME, ERROR_CODE_TAG, metricTag, code));
  }

  public void recordComposite(List<CalculationCommand> commands, CompositeCalculationResult result) {
    Map<CalculationMetric, BaseCalculationResult> succeeded = resultsOf(result);
    Map<CalculationMetric, List<Notification>> failed = failuresOf(result);

    for (CalculationCommand command : safe(commands)) {
      CalculationMetric metric = command == null ? null : command.getMetric();
      String metricTag = metricTag(metric);
      RequestShape shape = RequestShape.of(command);

      if (metric != null && succeeded.containsKey(metric)) {
        recordMetricSuccess(metricTag, warningsOf(succeeded.get(metric)), shape);
      } else if (metric != null && failed.containsKey(metric)) {
        recordMemberFailure(metricTag, failed.get(metric), shape);
      } else {
        recordMemberFailure(metricTag, List.of(), shape);
      }
    }
  }

  public void recordCompositeFailure(List<CalculationCommand> commands, Throwable cause) {
    List<String> errorCodes = errorCodesOf(cause);
    for (CalculationCommand command : safe(commands)) {
      String metricTag = metricTag(command == null ? null : command.getMetric());
      countExecution(metricTag, ERROR);
      recordShape(metricTag, RequestShape.of(command));
      errorCodes.forEach(code -> countCode(ERROR_CODES_METER_NAME, ERROR_CODE_TAG, metricTag, code));
    }
  }

  private void recordMetricSuccess(String metricTag, List<Notification> warnings, RequestShape shape) {
    countExecution(metricTag, SUCCESS);
    recordShape(metricTag, shape);
    recordWarningCount(metricTag, warnings == null ? 0 : warnings.size());
    codesOf(warnings).forEach(code -> countCode(WARNING_CODES_METER_NAME, WARNING_CODE_TAG, metricTag, code));
  }

  private void recordMemberFailure(String metricTag, List<Notification> notifications, RequestShape shape) {
    countExecution(metricTag, ERROR);
    recordShape(metricTag, shape);

    List<String> errorCodes = codesBySeverity(notifications, false);
    if (errorCodes.isEmpty()) {
      countCode(ERROR_CODES_METER_NAME, ERROR_CODE_TAG, metricTag, UNMAPPED);
    } else {
      errorCodes.forEach(code -> countCode(ERROR_CODES_METER_NAME, ERROR_CODE_TAG, metricTag, code));
    }
    codesBySeverity(notifications, true)
        .forEach(code -> countCode(WARNING_CODES_METER_NAME, WARNING_CODE_TAG, metricTag, code));
  }

  private void recordDuration(String metricTag, String outcome, Duration duration) {
    if (duration == null) {
      return;
    }
    Timer.builder(DURATION_METER_NAME)
        .description("Time one calculation metric took to compute, excluding request validation and data fetching")
        .tag(METRIC_TAG, metricTag)
        .tag(OUTCOME_TAG, outcome)
        .register(meterRegistry)
        .record(duration);
  }

  private void countExecution(String metricTag, String outcome) {
    Counter.builder(EXECUTIONS_METER_NAME)
        .description("Calculation executions per metric, split by outcome")
        .tag(METRIC_TAG, metricTag)
        .tag(OUTCOME_TAG, outcome)
        .register(meterRegistry)
        .increment();
  }

  private void countCode(String meterName, String codeTag, String metricTag, String code) {
    Counter.builder(meterName)
        .description("Occurrences of each code produced per calculation metric")
        .tag(METRIC_TAG, metricTag)
        .tag(codeTag, code)
        .register(meterRegistry)
        .increment();
  }

  private void recordWarningCount(String metricTag, int warnings) {
    DistributionSummary.builder(WARNINGS_METER_NAME)
        .description("Warnings attached to a successful calculation result")
        .baseUnit("warnings")
        .tag(METRIC_TAG, metricTag)
        .register(meterRegistry)
        .record(warnings);
    trackMinWarnings(metricTag, warnings);
  }

  private void trackMinWarnings(String metricTag, int warnings) {
    minWarningsByMetric.computeIfAbsent(metricTag, key -> {
      AtomicLong holder = new AtomicLong(warnings);
      Gauge.builder(WARNINGS_MIN_METER_NAME, holder, AtomicLong::get)
          .description("Fewest warnings seen on a single successful calculation of this metric")
          .baseUnit("warnings")
          .tag(METRIC_TAG, key)
          .register(meterRegistry);
      return holder;
    }).accumulateAndGet(warnings, Math::min);
  }

  private void recordShape(String metricTag, RequestShape shape) {
    RequestShape recorded = shape == null ? RequestShape.EMPTY : shape;
    DistributionSummary.builder(HOLDINGS_METER_NAME)
        .description("Portfolio holdings submitted per calculation of this metric")
        .baseUnit("holdings")
        .tag(METRIC_TAG, metricTag)
        .register(meterRegistry)
        .record(recorded.holdings());
    DistributionSummary.builder(BENCHMARK_HOLDINGS_METER_NAME)
        .description("Benchmark holdings submitted per calculation of this metric")
        .baseUnit("holdings")
        .tag(METRIC_TAG, metricTag)
        .register(meterRegistry)
        .record(recorded.benchmarkHoldings());
  }

  static List<String> errorCodesOf(Throwable cause) {
    if (cause instanceof CalculationsFailedException aggregate) {
      List<String> codes = aggregate.getExceptions().stream()
          .filter(Objects::nonNull)
          .map(BasePceException::getErrorCode)
          .filter(Objects::nonNull)
          .map(ErrorCode::getCode)
          .toList();
      return codes.isEmpty() ? List.of(simpleName(cause)) : codes;
    }
    if (cause instanceof BasePceException pceException && pceException.getErrorCode() != null) {
      return List.of(pceException.getErrorCode().getCode());
    }
    return List.of(simpleName(cause));
  }

  private static List<String> codesOf(List<Notification> notifications) {
    if (CollectionUtils.isEmpty(notifications)) {
      return List.of();
    }
    return notifications.stream()
        .filter(Objects::nonNull)
        .map(CalculationMetricStatistics::codeOf)
        .toList();
  }

  private static List<String> codesBySeverity(List<Notification> notifications, boolean warningsOnly) {
    if (CollectionUtils.isEmpty(notifications)) {
      return List.of();
    }
    return notifications.stream()
        .filter(Objects::nonNull)
        .filter(notification -> warningsOnly == (notification.getSeverity() == Severity.WARNING))
        .map(CalculationMetricStatistics::codeOf)
        .toList();
  }

  private static String codeOf(Notification notification) {
    String code = notification.getCode();
    return code == null || code.isBlank() ? UNMAPPED : code;
  }

  private static String simpleName(Throwable cause) {
    return cause == null ? UNKNOWN : cause.getClass().getSimpleName();
  }

  private static String metricTag(CalculationMetric metric) {
    return metric == null ? UNKNOWN : metric.getValue();
  }

  private static List<Notification> warningsOf(BaseCalculationResult result) {
    return result == null ? List.of() : result.getWarnings();
  }

  private static Map<CalculationMetric, BaseCalculationResult> resultsOf(CompositeCalculationResult result) {
    return result == null || result.getResults() == null ? Map.of() : result.getResults();
  }

  private static Map<CalculationMetric, List<Notification>> failuresOf(CompositeCalculationResult result) {
    return result == null || result.getFailures() == null ? Map.of() : result.getFailures();
  }

  private static List<CalculationCommand> safe(List<CalculationCommand> commands) {
    return commands == null ? List.of() : commands;
  }
}
