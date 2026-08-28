package ca.tangerine.pce.observability.calculation;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.result.composite.CompositeCalculationResult;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.CompositeCalculationRequest;
import ca.tangerine.pce.port.observability.CalculationObservability;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Micrometer implementation of {@link CalculationObservability}: traces every calculation request as one observation
 * and delegates the per-metric numbers to {@link CalculationMetricStatistics}.
 *
 * <p>
 * Both endpoints share the observation name {@code portfolio.calculation.request} and the same low-cardinality tag set,
 * so the derived timer measures request latency without caring which endpoint was used and without ever tagging a
 * request as a metric. Which metrics a request covered is recorded as span attributes only; the numbers per metric —
 * counts, durations, warnings, codes — live in the dedicated meters.
 *
 * <p>
 * What the request asked for — metrics and holding counts — is attached before dispatch and what came back after, so a
 * failed request carries the same attributes as a successful one and a trace can be read without knowing the outcome
 * first.
 *
 * <p>
 * Everything observed here has already passed metric resolution and request validation, which the port requires of its
 * callers. An exception reaching these catch blocks therefore comes from a calculation that was actually attempted,
 * which is what keeps a rejected request out of the per-metric failure counts and out of the error-code ranking.
 */
@Component
@RequiredArgsConstructor
public class MicrometerCalculationObservability implements CalculationObservability {

  static final String REQUEST_OBSERVATION_NAME = "portfolio.calculation.request";

  static final String UNKNOWN = "unknown";
  static final String UNSUPPORTED = "unsupported";
  static final String COMPOSITE = "composite";
  static final String NONE = "none";
  static final String SUCCESS = "success";
  static final String ERROR = "error";

  static final String COMMAND_TAG = "command.type";
  static final String OUTCOME_TAG = "outcome";
  static final String ERROR_TYPE_TAG = "error.type";
  static final String RESULT_TAG = "result.type";

  static final String REQUESTED_METRIC_KEY = "requested.metric";
  static final String PORTFOLIO_HOLDINGS_COUNT_KEY = "portfolio.holdings.count";
  static final String BENCHMARK_HOLDINGS_COUNT_KEY = "benchmark.holdings.count";
  static final String WARNINGS_COUNT_KEY = "warnings.count";
  static final String FAILED_METRICS_COUNT_KEY = "failed.metrics.count";
  static final String REQUESTED_METRICS_COUNT_KEY = "requested.metrics.count";

  private static final Set<String> SUPPORTED_METRIC_VALUES = Arrays.stream(CalculationMetric.values())
      .map(CalculationMetric::getValue)
      .collect(Collectors.toUnmodifiableSet());

  private final ObservationRegistry observationRegistry;
  private final CalculationMetricStatistics statistics;

  @Override
  public BaseCalculationResult observe(
      String metricName,
      CalculationCommand command,
      Supplier<BaseCalculationResult> action) {
    String metricTag = resolveMetricTag(metricName);
    RequestShape shape = RequestShape.of(command);

    Observation observation = Observation.createNotStarted(REQUEST_OBSERVATION_NAME, observationRegistry)
        .contextualName("portfolio " + metricTag + " calculation")
        .lowCardinalityKeyValue(COMMAND_TAG, commandType(command))
        .highCardinalityKeyValue(REQUESTED_METRIC_KEY, valueOrUnknown(metricName))
        .highCardinalityKeyValue(REQUESTED_METRICS_COUNT_KEY, "1")
        .highCardinalityKeyValue(PORTFOLIO_HOLDINGS_COUNT_KEY, String.valueOf(shape.holdings()))
        .highCardinalityKeyValue(BENCHMARK_HOLDINGS_COUNT_KEY, String.valueOf(shape.benchmarkHoldings()));

    observation.start();
    try (Observation.Scope ignored = observation.openScope()) {
      BaseCalculationResult result = action.get();
      observation.lowCardinalityKeyValue(OUTCOME_TAG, SUCCESS);
      observation.lowCardinalityKeyValue(ERROR_TYPE_TAG, NONE);
      observation.lowCardinalityKeyValue(RESULT_TAG, resultType(result));
      observation.highCardinalityKeyValue(WARNINGS_COUNT_KEY, String.valueOf(warningsCount(result)));
      observation.highCardinalityKeyValue(FAILED_METRICS_COUNT_KEY, "0");
      statistics.recordSingleSuccess(metricTag, result, shape);
      return result;
    } catch (RuntimeException exception) {
      observation.error(exception);
      observation.lowCardinalityKeyValue(OUTCOME_TAG, ERROR);
      observation.lowCardinalityKeyValue(ERROR_TYPE_TAG, exception.getClass().getSimpleName());
      observation.lowCardinalityKeyValue(RESULT_TAG, NONE);
      observation.highCardinalityKeyValue(WARNINGS_COUNT_KEY, "0");
      observation.highCardinalityKeyValue(FAILED_METRICS_COUNT_KEY, "1");
      statistics.recordSingleFailure(metricTag, exception, shape);
      throw exception;
    } finally {
      observation.stop();
    }
  }

  @Override
  public CompositeCalculationResult observeComposite(
      List<CalculationCommand> commands,
      Supplier<CompositeCalculationResult> action) {
    List<CalculationCommand> observedCommands = commands == null ? List.of() : commands;
    RequestShape shape = compositeShape(observedCommands);

    Observation observation = Observation.createNotStarted(REQUEST_OBSERVATION_NAME, observationRegistry)
        .contextualName("portfolio " + COMPOSITE + " calculation")
        .lowCardinalityKeyValue(COMMAND_TAG, CompositeCalculationRequest.class.getSimpleName())
        .highCardinalityKeyValue(REQUESTED_METRIC_KEY, requestedMetrics(observedCommands))
        .highCardinalityKeyValue(REQUESTED_METRICS_COUNT_KEY, String.valueOf(observedCommands.size()))
        .highCardinalityKeyValue(PORTFOLIO_HOLDINGS_COUNT_KEY, String.valueOf(shape.holdings()))
        .highCardinalityKeyValue(BENCHMARK_HOLDINGS_COUNT_KEY, String.valueOf(shape.benchmarkHoldings()));

    observation.start();
    try (Observation.Scope ignored = observation.openScope()) {
      CompositeCalculationResult result = action.get();
      observation.lowCardinalityKeyValue(OUTCOME_TAG, SUCCESS);
      observation.lowCardinalityKeyValue(ERROR_TYPE_TAG, NONE);
      observation.lowCardinalityKeyValue(RESULT_TAG, resultType(result));
      observation.highCardinalityKeyValue(WARNINGS_COUNT_KEY, String.valueOf(warningsCount(result)));
      observation.highCardinalityKeyValue(FAILED_METRICS_COUNT_KEY, String.valueOf(failuresCount(result)));
      statistics.recordComposite(observedCommands, result);
      return result;
    } catch (RuntimeException exception) {
      observation.error(exception);
      observation.lowCardinalityKeyValue(OUTCOME_TAG, ERROR);
      observation.lowCardinalityKeyValue(ERROR_TYPE_TAG, exception.getClass().getSimpleName());
      observation.lowCardinalityKeyValue(RESULT_TAG, NONE);
      observation.highCardinalityKeyValue(WARNINGS_COUNT_KEY, "0");
      observation.highCardinalityKeyValue(FAILED_METRICS_COUNT_KEY, String.valueOf(observedCommands.size()));
      statistics.recordCompositeFailure(observedCommands, exception);
      throw exception;
    } finally {
      observation.stop();
    }
  }

  private static RequestShape compositeShape(List<CalculationCommand> commands) {
    return commands.stream()
        .map(RequestShape::of)
        .reduce(RequestShape.EMPTY, RequestShape::plus);
  }

  private static String requestedMetrics(List<CalculationCommand> commands) {
    if (commands.isEmpty()) {
      return UNKNOWN;
    }
    return commands.stream()
        .map(MicrometerCalculationObservability::bodyMetric)
        .collect(Collectors.joining(","));
  }

  private static String resultType(CompositeCalculationResult result) {
    return result == null ? UNKNOWN : valueOrUnknown(result.getClass().getSimpleName());
  }

  private static int warningsCount(CompositeCalculationResult result) {
    if (result == null || CollectionUtils.isEmpty(result.getResults())) {
      return 0;
    }
    return result.getResults().values().stream()
        .mapToInt(MicrometerCalculationObservability::warningsCount)
        .sum();
  }

  private static int failuresCount(CompositeCalculationResult result) {
    return result == null || CollectionUtils.isEmpty(result.getFailures()) ? 0 : result.getFailures().size();
  }

  private static String resolveMetricTag(String metricName) {
    return SUPPORTED_METRIC_VALUES.contains(metricName) ? metricName : UNSUPPORTED;
  }

  private static String commandType(CalculationCommand command) {
    return command == null ? UNKNOWN : command.getClass().getSimpleName();
  }

  private static String bodyMetric(CalculationCommand command) {
    return command == null || command.getMetric() == null ? UNKNOWN : command.getMetric().getValue();
  }

  private static String valueOrUnknown(String value) {
    return value == null || value.isBlank() ? UNKNOWN : value;
  }

  private static String resultType(BaseCalculationResult result) {
    return result == null ? UNKNOWN : valueOrUnknown(result.getClass().getSimpleName());
  }

  private static int warningsCount(BaseCalculationResult result) {
    return result == null || CollectionUtils.isEmpty(result.getWarnings()) ? 0 : result.getWarnings().size();
  }
}
