package com.fintex.ce.application.calculation.observability;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.composite.CompositeCalculationResult;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationMetricStatisticsTest {

  private static final String ALPHA = CalculationMetric.ALPHA.getValue();
  private static final String BETA = CalculationMetric.BETA.getValue();
  private static final String SHARPE = CalculationMetric.SHARPE_RATIO.getValue();

  private final SimpleMeterRegistry meterRegistry = ConfiguredMeterRegistry.withPercentiles();
  private final CalculationMetricStatistics statistics = new CalculationMetricStatistics(meterRegistry);

  @Test
  void shouldRecordSuccessAndWarningCodes_whenSingleCalculationCompletes() {
    BaseCalculationResult result = resultWith(warning("FDS-001"), warning("FDS-001"), warning("FDS-002"));

    statistics.recordSingleSuccess(ALPHA, result, new RequestShape(7, 2));

    assertThat(executions(ALPHA, CalculationMetricStatistics.SUCCESS))
        .isEqualTo(1);
    assertThat(executions(ALPHA, CalculationMetricStatistics.ERROR)).isZero();
    assertThat(warningCode(ALPHA, "FDS-001")).isEqualTo(2);
    assertThat(warningCode(ALPHA, "FDS-002")).isEqualTo(1);
    assertThat(warnings(ALPHA)).isNotNull();
    assertThat(warnings(ALPHA).count()).isEqualTo(1);
    assertThat(warnings(ALPHA).totalAmount()).isEqualTo(3.0);
    assertThat(warnings(ALPHA).max()).isEqualTo(3.0);
    assertThat(warningsMin(ALPHA)).isEqualTo(3.0);
    assertThat(holdings(ALPHA).max()).isEqualTo(7.0);
    assertThat(benchmarkHoldings(ALPHA).max()).isEqualTo(2.0);
  }

  @Test
  void shouldTrackMinMeanAndMaxWarnings_whenMetricRunsSeveralTimes() {
    statistics.recordSingleSuccess(ALPHA, resultWith(warning("FDS-001")), new RequestShape(1, 0));
    statistics.recordSingleSuccess(ALPHA, resultWith(), new RequestShape(1, 0));
    statistics.recordSingleSuccess(ALPHA,
        resultWith(warning("FDS-001"), warning("FDS-001"), warning("FDS-001"), warning("FDS-001")),
        new RequestShape(1, 0));

    assertThat(warnings(ALPHA).count()).isEqualTo(3);
    assertThat(warnings(ALPHA).totalAmount()).isEqualTo(5.0);
    assertThat(warnings(ALPHA).mean()).isEqualTo(5.0 / 3);
    assertThat(warnings(ALPHA).max()).isEqualTo(4.0);
    assertThat(warningsMin(ALPHA)).isZero();
    assertThat(warningCode(ALPHA, "FDS-001")).isEqualTo(5);
  }

  @Test
  void shouldRecordEachMemberMetricSeparately_whenCompositeRequestPartiallyFails() {
    List<CalculationCommand> commands = List.of(
        command(CalculationMetric.ALPHA, 4, 1),
        command(CalculationMetric.BETA, 4, 1),
        command(CalculationMetric.SHARPE_RATIO, 4, 1));

    Map<CalculationMetric, BaseCalculationResult> results = new LinkedHashMap<>();
    results.put(CalculationMetric.ALPHA, resultWith(warning("FDS-001"), warning("FDS-001")));
    results.put(CalculationMetric.BETA, resultWith());

    CompositeCalculationResult result = CompositeCalculationResult.builder()
        .results(results)
        .failures(Map.of(CalculationMetric.SHARPE_RATIO, List.of(error("CALC-100"), warning("FDS-002"))))
        .build();

    statistics.recordComposite(commands, result);

    assertThat(meterRegistry.find(CalculationMetricStatistics.EXECUTIONS_METER_NAME)
        .tag(CalculationMetricStatistics.METRIC_TAG, "composite")
        .counters())
        .as("a composite request must never be counted as a metric of its own")
        .isEmpty();

    assertThat(executions(ALPHA, CalculationMetricStatistics.SUCCESS))
        .isEqualTo(1);
    assertThat(executions(BETA, CalculationMetricStatistics.SUCCESS))
        .isEqualTo(1);
    assertThat(executions(SHARPE, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    assertThat(executions(SHARPE, CalculationMetricStatistics.SUCCESS))
        .isZero();

    assertThat(errorCode(SHARPE, "CALC-100")).isEqualTo(1);
    assertThat(warningCode(SHARPE, "FDS-002")).isEqualTo(1);
    assertThat(warningCode(ALPHA, "FDS-001")).isEqualTo(2);

    assertThat(warnings(ALPHA).max()).isEqualTo(2.0);
    assertThat(warnings(BETA).count()).isEqualTo(1);
    assertThat(warnings(BETA).totalAmount()).isZero();
    assertThat(warnings(SHARPE))
        .as("a failed metric contributes no sample to the warning distribution")
        .isNull();
    assertThat(holdings(SHARPE).max()).isEqualTo(4.0);
  }

  @Test
  void shouldRecordUnmappedErrorCode_whenCompositeMemberFailsWithoutNotifications() {
    List<CalculationCommand> commands = List.of(command(CalculationMetric.ALPHA, 2, 0));
    CompositeCalculationResult result = CompositeCalculationResult.builder()
        .results(Map.of())
        .failures(Map.of())
        .build();

    statistics.recordComposite(commands, result);

    assertThat(executions(ALPHA, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    assertThat(errorCode(ALPHA, CalculationMetricStatistics.UNMAPPED)).isEqualTo(1);
  }

  @Test
  void shouldFailEveryRequestedMetric_whenWholeCompositeRequestThrows() {
    List<CalculationCommand> commands = List.of(
        command(CalculationMetric.ALPHA, 3, 0),
        command(CalculationMetric.BETA, 3, 0));

    statistics.recordCompositeFailure(commands, ErrorCode.METRIC_MISMATCH.toException("alpha", "beta"));

    assertThat(executions(ALPHA, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    assertThat(executions(BETA, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    assertThat(errorCode(ALPHA, ErrorCode.METRIC_MISMATCH.getCode())).isEqualTo(1);
    assertThat(errorCode(BETA, ErrorCode.METRIC_MISMATCH.getCode())).isEqualTo(1);
    assertThat(warnings(ALPHA)).isNull();
  }

  static Stream<Arguments> failures() {
    return Stream.of(
        Arguments.of("domain error carries its code",
            ErrorCode.METRIC_MISMATCH.toException("alpha", "beta"),
            List.of(ErrorCode.METRIC_MISMATCH.getCode())),
        Arguments.of("aggregated domain errors carry every code",
            new CalculationsFailedException(List.of(
                ErrorCode.METRIC_MISMATCH.toException("alpha", "beta"),
                ErrorCode.METRIC_REQUIRED.toException())),
            List.of(ErrorCode.METRIC_MISMATCH.getCode(), ErrorCode.METRIC_REQUIRED.getCode())),
        Arguments.of("untyped failure falls back to the exception name",
            new IllegalStateException("boom"),
            List.of("IllegalStateException")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("failures")
  void shouldRecordErrorCodes_whenSingleCalculationFails(
      String scenario,
      Throwable cause,
      List<String> expectedCodes) {
    statistics.recordSingleFailure(ALPHA, cause, new RequestShape(5, 0));

    assertThat(executions(ALPHA, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    expectedCodes.forEach(code -> assertThat(errorCode(ALPHA, code)).isEqualTo(1));
    assertThat(meterRegistry.find(CalculationMetricStatistics.ERROR_CODES_METER_NAME)
        .tag(CalculationMetricStatistics.METRIC_TAG, ALPHA)
        .counters())
        .hasSize(expectedCodes.size());
    assertThat(holdings(ALPHA).max()).isEqualTo(5.0);
  }

  @Test
  void shouldTimeEachMetricSeparately_whenOrchestratorReportsDurations() {
    statistics.recordSuccess(CalculationMetric.ALPHA, Duration.ofMillis(40));
    statistics.recordSuccess(CalculationMetric.ALPHA, Duration.ofMillis(60));
    statistics.recordFailure(CalculationMetric.ALPHA, Duration.ofMillis(5));
    statistics.recordSuccess(CalculationMetric.BETA, Duration.ofMillis(200));

    Timer alphaSuccess = duration(ALPHA, CalculationMetricStatistics.SUCCESS);
    assertThat(alphaSuccess).isNotNull();
    assertThat(alphaSuccess.count()).isEqualTo(2);
    assertThat(alphaSuccess.mean(TimeUnit.MILLISECONDS)).isEqualTo(50.0);
    assertThat(alphaSuccess.max(TimeUnit.MILLISECONDS)).isEqualTo(60.0);
    assertThat(alphaSuccess.takeSnapshot().percentileValues())
        .extracting(ValueAtPercentile::percentile)
        .containsExactly(0.5, 0.95, 0.99);

    Timer alphaFailure = duration(ALPHA, CalculationMetricStatistics.ERROR);
    assertThat(alphaFailure).isNotNull();
    assertThat(alphaFailure.count()).isEqualTo(1);
    assertThat(alphaFailure.max(TimeUnit.MILLISECONDS))
        .as("a fast failure must not be mixed into the successful-latency distribution")
        .isEqualTo(5.0);

    assertThat(duration(BETA, CalculationMetricStatistics.SUCCESS).max(TimeUnit.MILLISECONDS)).isEqualTo(200.0);
    assertThat(duration(SHARPE, CalculationMetricStatistics.SUCCESS)).isNull();
  }

  @Test
  void shouldTagDurationAsUnknown_whenMetricIsMissing() {
    statistics.recordSuccess(null, Duration.ofMillis(10));

    assertThat(duration(CalculationMetricStatistics.UNKNOWN, CalculationMetricStatistics.SUCCESS)).isNotNull();
  }

  private Timer duration(String metric, String outcome) {
    return meterRegistry.find(CalculationMetricStatistics.DURATION_METER_NAME)
        .tag(CalculationMetricStatistics.METRIC_TAG, metric)
        .tag(CalculationMetricStatistics.OUTCOME_TAG, outcome)
        .timer();
  }

  private double executions(String metric, String outcome) {
    Counter counter = meterRegistry.find(CalculationMetricStatistics.EXECUTIONS_METER_NAME)
        .tag(CalculationMetricStatistics.METRIC_TAG, metric)
        .tag(CalculationMetricStatistics.OUTCOME_TAG, outcome)
        .counter();
    return counter == null ? 0 : counter.count();
  }

  private double errorCode(String metric, String code) {
    return codeCount(CalculationMetricStatistics.ERROR_CODES_METER_NAME,
        CalculationMetricStatistics.ERROR_CODE_TAG, metric, code);
  }

  private double warningCode(String metric, String code) {
    return codeCount(CalculationMetricStatistics.WARNING_CODES_METER_NAME,
        CalculationMetricStatistics.WARNING_CODE_TAG, metric, code);
  }

  private double codeCount(String meterName, String codeTag, String metric, String code) {
    Counter counter = meterRegistry.find(meterName)
        .tag(CalculationMetricStatistics.METRIC_TAG, metric)
        .tag(codeTag, code)
        .counter();
    return counter == null ? 0 : counter.count();
  }

  private DistributionSummary warnings(String metric) {
    return summary(CalculationMetricStatistics.WARNINGS_METER_NAME, metric);
  }

  private DistributionSummary holdings(String metric) {
    return summary(CalculationMetricStatistics.HOLDINGS_METER_NAME, metric);
  }

  private DistributionSummary benchmarkHoldings(String metric) {
    return summary(CalculationMetricStatistics.BENCHMARK_HOLDINGS_METER_NAME, metric);
  }

  private DistributionSummary summary(String meterName, String metric) {
    return meterRegistry.find(meterName)
        .tag(CalculationMetricStatistics.METRIC_TAG, metric)
        .summary();
  }

  private double warningsMin(String metric) {
    Gauge gauge = meterRegistry.find(CalculationMetricStatistics.WARNINGS_MIN_METER_NAME)
        .tag(CalculationMetricStatistics.METRIC_TAG, metric)
        .gauge();
    return gauge == null ? -1 : gauge.value();
  }

  private static BaseCalculationResult resultWith(Notification... notifications) {
    BaseCalculationResult result = new BaseCalculationResult() {};
    result.setWarnings(List.of(notifications));
    return result;
  }

  private static Notification warning(String code) {
    return Notification.builder().code(code).severity(Severity.WARNING).message(code).build();
  }

  private static Notification error(String code) {
    return Notification.builder().code(code).severity(Severity.ERROR).message(code).build();
  }

  private static CalculationCommand command(CalculationMetric metric, int holdings, int benchmarkHoldings) {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(metric);
    command.setCurrency(Currency.CAD);
    command.setPeriods(Set.of(TimePeriod.ONE_YR));
    command.setHoldings(holdingList(holdings));
    command.setBenchmarkHoldings(holdingList(benchmarkHoldings));
    return command;
  }

  private static List<PortfolioHolding> holdingList(int size) {
    return IntStream.range(0, size)
        .mapToObj(index -> new PortfolioHolding(
            BigDecimal.ONE,
            FinancialInstrumentType.ETF,
            Country.CANADA,
            new SecurityIdentifier("TICKER-" + index, FiIdentifierType.TICKER)))
        .toList();
  }
}
