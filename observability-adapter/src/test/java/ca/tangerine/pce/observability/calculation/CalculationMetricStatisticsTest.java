package ca.tangerine.pce.observability.calculation;

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

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.etf;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.result.composite.CompositeCalculationResult;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.CalculationsFailedException;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

class CalculationMetricStatisticsTest {

  private static final String MAX_DRAWDOWN = CalculationMetric.MAX_DRAWDOWN.getValue();
  private static final String STANDARD_DEVIATION = CalculationMetric.STANDARD_DEVIATION.getValue();
  private static final String SHARPE = CalculationMetric.SHARPE_RATIO.getValue();

  // Real catalog codes, referenced through Codes so a renumbering cannot silently strand these fixtures on a
  // code that no longer exists. Both are warnings, which is what the warning counters record.
  private static final String SECTOR_WARNING = ErrorCode.Codes.MISSING_EQUITY_SECTOR_ALLOCATION;
  private static final String TBILL_ERROR = ErrorCode.Codes.MISSING_TBILL_RATE;
  private static final String MER_WARNING = ErrorCode.Codes.MISSING_MANAGEMENT_EXPENSE_RATIO;

  private final SimpleMeterRegistry meterRegistry = ConfiguredMeterRegistry.withPercentiles();
  private final CalculationMetricStatistics statistics = new CalculationMetricStatistics(meterRegistry);

  @Test
  void shouldRecordSuccessAndWarningCodes_whenSingleCalculationCompletes() {
    BaseCalculationResult result = resultWith(warning(SECTOR_WARNING), warning(SECTOR_WARNING), warning(MER_WARNING));

    statistics.recordSingleSuccess(MAX_DRAWDOWN, result, new RequestShape(7, 2));

    assertThat(executions(MAX_DRAWDOWN, CalculationMetricStatistics.SUCCESS))
        .isEqualTo(1);
    assertThat(executions(MAX_DRAWDOWN, CalculationMetricStatistics.ERROR)).isZero();
    assertThat(warningCode(MAX_DRAWDOWN, SECTOR_WARNING)).isEqualTo(2);
    assertThat(warningCode(MAX_DRAWDOWN, MER_WARNING)).isEqualTo(1);
    assertThat(warnings(MAX_DRAWDOWN)).isNotNull();
    assertThat(warnings(MAX_DRAWDOWN).count()).isEqualTo(1);
    assertThat(warnings(MAX_DRAWDOWN).totalAmount()).isEqualTo(3.0);
    assertThat(warnings(MAX_DRAWDOWN).max()).isEqualTo(3.0);
    assertThat(warningsMin(MAX_DRAWDOWN)).isEqualTo(3.0);
    assertThat(holdings(MAX_DRAWDOWN).max()).isEqualTo(7.0);
    assertThat(benchmarkHoldings(MAX_DRAWDOWN).max()).isEqualTo(2.0);
  }

  @Test
  void shouldTrackMinMeanAndMaxWarnings_whenMetricRunsSeveralTimes() {
    statistics.recordSingleSuccess(MAX_DRAWDOWN, resultWith(warning(SECTOR_WARNING)), new RequestShape(1, 0));
    statistics.recordSingleSuccess(MAX_DRAWDOWN, resultWith(), new RequestShape(1, 0));
    statistics.recordSingleSuccess(MAX_DRAWDOWN,
        resultWith(warning(SECTOR_WARNING), warning(SECTOR_WARNING), warning(SECTOR_WARNING), warning(SECTOR_WARNING)),
        new RequestShape(1, 0));

    assertThat(warnings(MAX_DRAWDOWN).count()).isEqualTo(3);
    assertThat(warnings(MAX_DRAWDOWN).totalAmount()).isEqualTo(5.0);
    assertThat(warnings(MAX_DRAWDOWN).mean()).isEqualTo(5.0 / 3);
    assertThat(warnings(MAX_DRAWDOWN).max()).isEqualTo(4.0);
    assertThat(warningsMin(MAX_DRAWDOWN)).isZero();
    assertThat(warningCode(MAX_DRAWDOWN, SECTOR_WARNING)).isEqualTo(5);
  }

  @Test
  void shouldRecordEachMemberMetricSeparately_whenCompositeRequestPartiallyFails() {
    List<CalculationCommand> commands = List.of(
        command(CalculationMetric.MAX_DRAWDOWN, 4, 1),
        command(CalculationMetric.STANDARD_DEVIATION, 4, 1),
        command(CalculationMetric.SHARPE_RATIO, 4, 1));

    Map<CalculationMetric, BaseCalculationResult> results = new LinkedHashMap<>();
    results.put(CalculationMetric.MAX_DRAWDOWN, resultWith(warning(SECTOR_WARNING), warning(SECTOR_WARNING)));
    results.put(CalculationMetric.STANDARD_DEVIATION, resultWith());

    CompositeCalculationResult result = CompositeCalculationResult.builder()
        .results(results)
        .failures(Map.of(CalculationMetric.SHARPE_RATIO, List.of(error(TBILL_ERROR), warning(MER_WARNING))))
        .build();

    statistics.recordComposite(commands, result);

    assertThat(meterRegistry.find(CalculationMetricStatistics.EXECUTIONS_METER_NAME)
        .tag(CalculationMetricStatistics.METRIC_TAG, "composite")
        .counters())
        .as("a composite request must never be counted as a metric of its own")
        .isEmpty();

    assertThat(executions(MAX_DRAWDOWN, CalculationMetricStatistics.SUCCESS))
        .isEqualTo(1);
    assertThat(executions(STANDARD_DEVIATION, CalculationMetricStatistics.SUCCESS))
        .isEqualTo(1);
    assertThat(executions(SHARPE, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    assertThat(executions(SHARPE, CalculationMetricStatistics.SUCCESS))
        .isZero();

    assertThat(errorCode(SHARPE, TBILL_ERROR)).isEqualTo(1);
    assertThat(warningCode(SHARPE, MER_WARNING)).isEqualTo(1);
    assertThat(warningCode(MAX_DRAWDOWN, SECTOR_WARNING)).isEqualTo(2);

    assertThat(warnings(MAX_DRAWDOWN).max()).isEqualTo(2.0);
    assertThat(warnings(STANDARD_DEVIATION).count()).isEqualTo(1);
    assertThat(warnings(STANDARD_DEVIATION).totalAmount()).isZero();
    assertThat(warnings(SHARPE))
        .as("a failed metric contributes no sample to the warning distribution")
        .isNull();
    assertThat(holdings(SHARPE).max()).isEqualTo(4.0);
  }

  @Test
  void shouldRecordUnmappedErrorCode_whenCompositeMemberFailsWithoutNotifications() {
    List<CalculationCommand> commands = List.of(command(CalculationMetric.MAX_DRAWDOWN, 2, 0));
    CompositeCalculationResult result = CompositeCalculationResult.builder()
        .results(Map.of())
        .failures(Map.of())
        .build();

    statistics.recordComposite(commands, result);

    assertThat(executions(MAX_DRAWDOWN, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    assertThat(errorCode(MAX_DRAWDOWN, CalculationMetricStatistics.UNMAPPED)).isEqualTo(1);
  }

  @Test
  void shouldFailEveryRequestedMetric_whenWholeCompositeRequestThrows() {
    List<CalculationCommand> commands = List.of(
        command(CalculationMetric.MAX_DRAWDOWN, 3, 0),
        command(CalculationMetric.STANDARD_DEVIATION, 3, 0));

    statistics.recordCompositeFailure(commands, ErrorCode.METRIC_MISMATCH.toException("alpha", "beta"));

    assertThat(executions(MAX_DRAWDOWN, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    assertThat(executions(STANDARD_DEVIATION, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    assertThat(errorCode(MAX_DRAWDOWN, ErrorCode.METRIC_MISMATCH.getCode())).isEqualTo(1);
    assertThat(errorCode(STANDARD_DEVIATION, ErrorCode.METRIC_MISMATCH.getCode())).isEqualTo(1);
    assertThat(warnings(MAX_DRAWDOWN)).isNull();
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
    statistics.recordSingleFailure(MAX_DRAWDOWN, cause, new RequestShape(5, 0));

    assertThat(executions(MAX_DRAWDOWN, CalculationMetricStatistics.ERROR))
        .isEqualTo(1);
    expectedCodes.forEach(code -> assertThat(errorCode(MAX_DRAWDOWN, code)).isEqualTo(1));
    assertThat(meterRegistry.find(CalculationMetricStatistics.ERROR_CODES_METER_NAME)
        .tag(CalculationMetricStatistics.METRIC_TAG, MAX_DRAWDOWN)
        .counters())
        .hasSize(expectedCodes.size());
    assertThat(holdings(MAX_DRAWDOWN).max()).isEqualTo(5.0);
  }

  @Test
  void shouldTimeEachMetricSeparately_whenOrchestratorReportsDurations() {
    statistics.recordSuccess(CalculationMetric.MAX_DRAWDOWN, Duration.ofMillis(40));
    statistics.recordSuccess(CalculationMetric.MAX_DRAWDOWN, Duration.ofMillis(60));
    statistics.recordFailure(CalculationMetric.MAX_DRAWDOWN, Duration.ofMillis(5));
    statistics.recordSuccess(CalculationMetric.STANDARD_DEVIATION, Duration.ofMillis(200));

    Timer alphaSuccess = duration(MAX_DRAWDOWN, CalculationMetricStatistics.SUCCESS);
    assertThat(alphaSuccess).isNotNull();
    assertThat(alphaSuccess.count()).isEqualTo(2);
    assertThat(alphaSuccess.mean(TimeUnit.MILLISECONDS)).isEqualTo(50.0);
    assertThat(alphaSuccess.max(TimeUnit.MILLISECONDS)).isEqualTo(60.0);
    assertThat(alphaSuccess.takeSnapshot().percentileValues())
        .extracting(ValueAtPercentile::percentile)
        .containsExactly(0.5, 0.95, 0.99);

    Timer alphaFailure = duration(MAX_DRAWDOWN, CalculationMetricStatistics.ERROR);
    assertThat(alphaFailure).isNotNull();
    assertThat(alphaFailure.count()).isEqualTo(1);
    assertThat(alphaFailure.max(TimeUnit.MILLISECONDS))
        .as("a fast failure must not be mixed into the successful-latency distribution")
        .isEqualTo(5.0);

    assertThat(duration(STANDARD_DEVIATION, CalculationMetricStatistics.SUCCESS).max(TimeUnit.MILLISECONDS)).isEqualTo(
        200.0);
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
        .mapToObj(index -> etf("TICKER-" + index, Country.CANADA, 1))
        .toList();
  }
}
