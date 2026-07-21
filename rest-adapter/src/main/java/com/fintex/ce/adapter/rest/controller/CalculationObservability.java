package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.composite.CompositeCalculationResult;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.CompositeCalculationRequest;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.dto.command.contract.BenchmarkHoldingsProvider;
import com.fintex.ce.model.dto.command.contract.HoldingsProvider;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Wraps every metric calculation in a Micrometer observation carrying metric, command and outcome tags. The action
 * receives the started {@link Observation} so the caller can emit phase events while validating and executing the
 * calculation.
 */
@Component
@RequiredArgsConstructor
public class CalculationObservability {

  static final String CALCULATION_OBSERVATION_NAME = "portfolio.calculation";

  static final String UNKNOWN = "unknown";
  static final String UNSUPPORTED = "unsupported";
  static final String COMPOSITE = "composite";
  static final String NONE = "none";
  static final String SUCCESS = "success";
  static final String ERROR = "error";

  static final String METRIC_TAG = "calculation.metric";
  static final String COMMAND_TAG = "command.type";
  static final String OUTCOME_TAG = "outcome";
  static final String EXCEPTION_TAG = "exception";
  static final String BODY_METRIC_TAG = "body.metric";
  static final String RESULT_TAG = "result.type";

  static final String REQUESTED_METRIC_KEY = "requested.metric";
  static final String PORTFOLIO_HOLDINGS_COUNT_KEY = "portfolio.holdings.count";
  static final String BENCHMARK_HOLDINGS_COUNT_KEY = "benchmark.holdings.count";
  static final String WARNINGS_COUNT_KEY = "warnings.count";
  static final String FAILED_METRICS_COUNT_KEY = "failed.metrics.count";

  static final String VALIDATION_STARTED_EVENT = "portfolio.calculation.validation.started";
  static final String VALIDATION_COMPLETED_EVENT = "portfolio.calculation.validation.completed";
  static final String SERVICE_STARTED_EVENT = "portfolio.calculation.service.started";
  static final String SERVICE_COMPLETED_EVENT = "portfolio.calculation.service.completed";
  static final String COMPLETED_EVENT = "portfolio.calculation.completed";
  static final String FAILED_EVENT = "portfolio.calculation.failed";

  private static final Set<String> SUPPORTED_METRIC_VALUES = Arrays.stream(CalculationMetric.values())
      .map(CalculationMetric::getValue)
      .collect(Collectors.toUnmodifiableSet());

  private final ObservationRegistry observationRegistry;

  public BaseCalculationResult observe(
      String metricName,
      CalculationCommand command,
      Function<Observation, BaseCalculationResult> action) {
    String metricTag = resolveMetricTag(metricName);
    String commandTag = commandType(command);

    Observation observation = Observation.createNotStarted(CALCULATION_OBSERVATION_NAME, observationRegistry)
        .contextualName("portfolio " + metricTag + " calculation")
        .lowCardinalityKeyValue(METRIC_TAG, metricTag)
        .lowCardinalityKeyValue(COMMAND_TAG, commandTag)
        .lowCardinalityKeyValue(BODY_METRIC_TAG, bodyMetric(command))
        .highCardinalityKeyValue(REQUESTED_METRIC_KEY, valueOrUnknown(metricName))
        .highCardinalityKeyValue(PORTFOLIO_HOLDINGS_COUNT_KEY, String.valueOf(holdingsCount(command)))
        .highCardinalityKeyValue(BENCHMARK_HOLDINGS_COUNT_KEY, String.valueOf(benchmarkHoldingsCount(command)));

    observation.start();
    try (Observation.Scope ignored = observation.openScope()) {
      BaseCalculationResult result = action.apply(observation);
      observation.lowCardinalityKeyValue(OUTCOME_TAG, SUCCESS);
      observation.lowCardinalityKeyValue(EXCEPTION_TAG, NONE);
      observation.lowCardinalityKeyValue(RESULT_TAG, resultType(result));
      observation.highCardinalityKeyValue(WARNINGS_COUNT_KEY, String.valueOf(warningsCount(result)));
      observation.event(Observation.Event.of(COMPLETED_EVENT));
      return result;
    } catch (RuntimeException exception) {
      observation.error(exception);
      observation.lowCardinalityKeyValue(OUTCOME_TAG, ERROR);
      observation.lowCardinalityKeyValue(EXCEPTION_TAG, exception.getClass().getSimpleName());
      observation.event(Observation.Event.of(FAILED_EVENT));
      throw exception;
    } finally {
      observation.stop();
    }
  }

  public CompositeCalculationResult observeComposite(
      List<CalculationCommand> commands,
      Function<Observation, CompositeCalculationResult> action) {
    List<CalculationCommand> observedCommands = commands == null ? List.of() : commands;

    Observation observation = Observation.createNotStarted(CALCULATION_OBSERVATION_NAME, observationRegistry)
        .contextualName("portfolio " + COMPOSITE + " calculation")
        .lowCardinalityKeyValue(METRIC_TAG, COMPOSITE)
        .lowCardinalityKeyValue(COMMAND_TAG, CompositeCalculationRequest.class.getSimpleName())
        .lowCardinalityKeyValue(BODY_METRIC_TAG, COMPOSITE)
        .highCardinalityKeyValue(REQUESTED_METRIC_KEY, requestedMetrics(observedCommands))
        .highCardinalityKeyValue(PORTFOLIO_HOLDINGS_COUNT_KEY, String.valueOf(observedCommands.stream()
            .mapToInt(CalculationObservability::holdingsCount)
            .sum()))
        .highCardinalityKeyValue(BENCHMARK_HOLDINGS_COUNT_KEY, String.valueOf(observedCommands.stream()
            .mapToInt(CalculationObservability::benchmarkHoldingsCount)
            .sum()));

    observation.start();
    try (Observation.Scope ignored = observation.openScope()) {
      CompositeCalculationResult result = action.apply(observation);
      observation.lowCardinalityKeyValue(OUTCOME_TAG, SUCCESS);
      observation.lowCardinalityKeyValue(EXCEPTION_TAG, NONE);
      observation.lowCardinalityKeyValue(RESULT_TAG, resultType(result));
      observation.highCardinalityKeyValue(WARNINGS_COUNT_KEY, String.valueOf(warningsCount(result)));
      observation.highCardinalityKeyValue(FAILED_METRICS_COUNT_KEY, String.valueOf(failuresCount(result)));
      observation.event(Observation.Event.of(COMPLETED_EVENT));
      return result;
    } catch (RuntimeException exception) {
      observation.error(exception);
      observation.lowCardinalityKeyValue(OUTCOME_TAG, ERROR);
      observation.lowCardinalityKeyValue(EXCEPTION_TAG, exception.getClass().getSimpleName());
      observation.event(Observation.Event.of(FAILED_EVENT));
      throw exception;
    } finally {
      observation.stop();
    }
  }

  private static String requestedMetrics(List<CalculationCommand> commands) {
    if (commands.isEmpty()) {
      return UNKNOWN;
    }
    return commands.stream()
        .map(CalculationObservability::bodyMetric)
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
        .mapToInt(CalculationObservability::warningsCount)
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

  private static int holdingsCount(CalculationCommand command) {
    if (command instanceof MultiplePortfoliosCommand multiplePortfoliosCommand) {
      return multiplePortfoliosHoldingsCount(multiplePortfoliosCommand);
    }
    if (command instanceof HoldingsProvider holdingsProvider) {
      return sizeOf(holdingsProvider.getHoldings());
    }
    return 0;
  }

  private static int multiplePortfoliosHoldingsCount(MultiplePortfoliosCommand command) {
    if (CollectionUtils.isEmpty(command.getPortfolios())) {
      return 0;
    }
    return command.getPortfolios().stream()
        .map(MultiplePortfoliosCommand.Portfolio::getHoldings)
        .mapToInt(CalculationObservability::sizeOf)
        .sum();
  }

  private static int benchmarkHoldingsCount(CalculationCommand command) {
    return command instanceof BenchmarkHoldingsProvider provider ? sizeOf(provider.getBenchmarkHoldings()) : 0;
  }

  private static int sizeOf(List<PortfolioHolding> holdings) {
    return CollectionUtils.isEmpty(holdings) ? 0 : holdings.size();
  }

  private static String resultType(BaseCalculationResult result) {
    return result == null ? UNKNOWN : valueOrUnknown(result.getClass().getSimpleName());
  }

  private static int warningsCount(BaseCalculationResult result) {
    return result == null || CollectionUtils.isEmpty(result.getWarnings()) ? 0 : result.getWarnings().size();
  }
}
