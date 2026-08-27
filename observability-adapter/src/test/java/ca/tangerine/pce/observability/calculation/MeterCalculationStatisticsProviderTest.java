package ca.tangerine.pce.observability.calculation;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.port.observability.CalculationStatisticsReport;
import ca.tangerine.pce.port.observability.CalculationStatisticsReport.MetricStatistics;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

class MeterCalculationStatisticsProviderTest {

  private static final String MAX_DRAWDOWN = CalculationMetric.MAX_DRAWDOWN.getValue();
  private static final String STANDARD_DEVIATION = CalculationMetric.STANDARD_DEVIATION.getValue();

  private static final ErrorCode SHARED_ERROR = ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE;
  private static final List<ErrorCode> ALPHA_ONLY_ERRORS = List.of(
      ErrorCode.BAD_INPUT, ErrorCode.DUPLICATE_METRIC, ErrorCode.DUPLICATE_HOLDING,
      ErrorCode.FIELD_NOT_NULL, ErrorCode.FIELD_NOT_BLANK);
  private static final List<ErrorCode> BETA_ONLY_ERRORS = List.of(
      ErrorCode.FIELD_NOT_EMPTY, ErrorCode.CPED_NOT_MONTH_END, ErrorCode.CPSD_NOT_MONTH_END,
      ErrorCode.CIPSD_NOT_MONTH_END, ErrorCode.COUNTRY_NOT_SUPPORTED);

  private static final String SECTOR_WARNING = ErrorCode.Codes.MISSING_EQUITY_SECTOR_ALLOCATION;
  private static final String MER_WARNING = ErrorCode.Codes.MISSING_MANAGEMENT_EXPENSE_RATIO;
  private static final String SHARED_WARNING = ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC;
  // Five distinct warnings per metric, and SHARED_WARNING distinct from all ten: the ranking assertions turn on
  // the counts, not on which metric would really raise which code.
  private static final List<String> ALPHA_ONLY_WARNINGS = List.of(
      ErrorCode.Codes.UNKNOWN_TYPE_FROM_DATA_POINT, ErrorCode.Codes.MISSING_EQUITY_COUNTRY_EXPOSURE,
      ErrorCode.Codes.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE, ErrorCode.Codes.MISSING_ASSET_ALLOCATION,
      ErrorCode.Codes.MISSING_BOND_COUNTRY_EXPOSURE);
  private static final List<String> BETA_ONLY_WARNINGS = List.of(
      ErrorCode.Codes.MISSING_FIXED_INCOME_BOND_SECTOR, ErrorCode.Codes.MISSING_NET_EXPENSE_RATIO,
      ErrorCode.Codes.MISSING_GROSS_EXPENSE_RATIO, ErrorCode.Codes.MISSING_BUSINESS_COUNTRY_CODE,
      ErrorCode.Codes.MISSING_HOLDING_IDENTIFIERS);

  private final SimpleMeterRegistry meterRegistry = ConfiguredMeterRegistry.withPercentiles();
  private final CalculationMetricStatistics statistics = new CalculationMetricStatistics(meterRegistry);
  private final MeterCalculationStatisticsProvider provider = new MeterCalculationStatisticsProvider(meterRegistry);

  @Test
  void shouldRankMostFailingMetricFirst_whenSeveralMetricsExecuted() {
    statistics.recordSingleSuccess(MAX_DRAWDOWN, resultWith(warning(SECTOR_WARNING)), new RequestShape(10, 1));
    statistics.recordSingleSuccess(MAX_DRAWDOWN, resultWith(warning(SECTOR_WARNING), warning(MER_WARNING)),
        new RequestShape(10, 1));
    statistics.recordSingleFailure(MAX_DRAWDOWN, ErrorCode.METRIC_MISMATCH.toException("alpha", "beta"),
        new RequestShape(10, 1));
    statistics.recordSingleFailure(STANDARD_DEVIATION, new IllegalStateException("boom"), new RequestShape(4, 0));
    statistics.recordSingleFailure(STANDARD_DEVIATION, new IllegalStateException("boom"), new RequestShape(4, 0));
    statistics.recordSingleFailure(STANDARD_DEVIATION, ErrorCode.METRIC_REQUIRED.toException(), new RequestShape(4, 0));
    statistics.recordSuccess(CalculationMetric.MAX_DRAWDOWN, Duration.ofMillis(30));
    statistics.recordSuccess(CalculationMetric.MAX_DRAWDOWN, Duration.ofMillis(70));

    CalculationStatisticsReport report = provider.statistics();

    assertThat(report.metrics()).extracting(MetricStatistics::metric).containsExactly(STANDARD_DEVIATION, MAX_DRAWDOWN);

    MetricStatistics beta = report.metrics().get(0);
    assertThat(beta.executions()).isEqualTo(3);
    assertThat(beta.successes()).isZero();
    assertThat(beta.failures()).isEqualTo(3);
    assertThat(beta.failureRatePercent()).isEqualTo(100.0);
    assertThat(beta.duration().samples())
        .as("a metric that only ever failed has no successful-latency samples")
        .isZero();
    assertThat(beta.topErrorCodes()).first()
        .satisfies(frequency -> {
          assertThat(frequency.code()).isEqualTo("IllegalStateException");
          assertThat(frequency.count()).isEqualTo(2);
        });

    MetricStatistics alpha = report.metrics().get(1);
    assertThat(alpha.executions()).isEqualTo(3);
    assertThat(alpha.successes()).isEqualTo(2);
    assertThat(alpha.failures()).isEqualTo(1);
    assertThat(alpha.failureRatePercent()).isEqualTo(33.33);
    assertThat(alpha.warnings().total()).isEqualTo(3);
    assertThat(alpha.warnings().min()).isEqualTo(1);
    assertThat(alpha.warnings().max()).isEqualTo(2);
    assertThat(alpha.warnings().mean()).isEqualTo(1.5);
    assertThat(alpha.duration().samples()).isEqualTo(2);
    assertThat(alpha.duration().meanMillis()).isEqualTo(50.0);
    assertThat(alpha.duration().maxMillis()).isEqualTo(70.0);
    assertThat(alpha.duration().p99Millis()).isGreaterThan(0.0);
    assertThat(alpha.topWarningCodes()).extracting("code").containsExactly(SECTOR_WARNING, MER_WARNING);
    assertThat(alpha.topErrorCodes()).extracting("code").containsExactly(ErrorCode.METRIC_MISMATCH.getCode());

    assertThat(report.overall().executions()).isEqualTo(6);
    assertThat(report.overall().successes()).isEqualTo(2);
    assertThat(report.overall().failures()).isEqualTo(4);
    assertThat(report.overall().failureRatePercent()).isEqualTo(66.67);
    assertThat(report.overall().warnings()).isEqualTo(3);
    assertThat(report.overall().topErrorCodes()).first()
        .satisfies(frequency -> assertThat(frequency.code()).isEqualTo("IllegalStateException"));
  }

  /**
   * The overall rankings must be built from the complete per-metric tallies. A code that sits just below the cut-off
   * for every metric individually can still be the single most frequent code in the service, and merging the truncated
   * per-metric lists would make it disappear from the one view that is supposed to surface it.
   */
  @Test
  void shouldRankOverallCodesFromCompleteTallies_whenACodeIsBelowTheCutOffForEveryMetric() {
    recordFailures(MAX_DRAWDOWN, ALPHA_ONLY_ERRORS, 3);
    recordFailures(STANDARD_DEVIATION, BETA_ONLY_ERRORS, 3);
    recordFailures(MAX_DRAWDOWN, List.of(SHARED_ERROR), 2);
    recordFailures(STANDARD_DEVIATION, List.of(SHARED_ERROR), 2);
    recordWarnings(MAX_DRAWDOWN, ALPHA_ONLY_WARNINGS, 3);
    recordWarnings(STANDARD_DEVIATION, BETA_ONLY_WARNINGS, 3);
    recordWarnings(MAX_DRAWDOWN, List.of(SHARED_WARNING), 2);
    recordWarnings(STANDARD_DEVIATION, List.of(SHARED_WARNING), 2);

    CalculationStatisticsReport report = provider.statistics();

    assertThat(codes(metric(report, MAX_DRAWDOWN).topErrorCodes()))
        .as("five codes beat it within this metric, so it is cut from the per-metric list")
        .hasSize(5)
        .doesNotContain(SHARED_ERROR.getCode());
    assertThat(codes(metric(report, STANDARD_DEVIATION).topErrorCodes()))
        .hasSize(5)
        .doesNotContain(SHARED_ERROR.getCode());
    assertThat(codes(metric(report, MAX_DRAWDOWN).topWarningCodes()))
        .hasSize(5)
        .doesNotContain(SHARED_WARNING);

    assertThat(report.overall().topErrorCodes()).first()
        .satisfies(frequency -> {
          assertThat(frequency.code())
              .as("summed across metrics it outranks every code that beat it individually")
              .isEqualTo(SHARED_ERROR.getCode());
          assertThat(frequency.count()).isEqualTo(4);
        });
    assertThat(report.overall().topWarningCodes()).first()
        .satisfies(frequency -> {
          assertThat(frequency.code()).isEqualTo(SHARED_WARNING);
          assertThat(frequency.count()).isEqualTo(4);
        });
    assertThat(report.overall().topErrorCodes()).hasSize(5);
    assertThat(report.overall().topWarningCodes()).hasSize(5);
  }

  @Test
  void shouldReturnEmptyReport_whenNoCalculationRecorded() {
    CalculationStatisticsReport report = provider.statistics();

    assertThat(report.metrics()).isEmpty();
    assertThat(report.overall().executions()).isZero();
    assertThat(report.overall().failures()).isZero();
    assertThat(report.overall().failureRatePercent()).isZero();
    assertThat(report.overall().topErrorCodes()).isEmpty();
    assertThat(report.overall().topWarningCodes()).isEmpty();
  }

  private void recordFailures(String metric, List<ErrorCode> errorCodes, int occurrences) {
    errorCodes.forEach(errorCode -> {
      for (int occurrence = 0; occurrence < occurrences; occurrence++) {
        statistics.recordSingleFailure(metric, errorCode.toException(), new RequestShape(1, 0));
      }
    });
  }

  private void recordWarnings(String metric, List<String> warningCodes, int occurrences) {
    warningCodes.forEach(code -> {
      for (int occurrence = 0; occurrence < occurrences; occurrence++) {
        statistics.recordSingleSuccess(metric, resultWith(warning(code)), new RequestShape(1, 0));
      }
    });
  }

  private static MetricStatistics metric(CalculationStatisticsReport report, String metric) {
    return report.metrics().stream()
        .filter(statistics -> statistics.metric().equals(metric))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no row for metric " + metric));
  }

  private static List<String> codes(List<CalculationStatisticsReport.CodeFrequency> frequencies) {
    return frequencies.stream().map(CalculationStatisticsReport.CodeFrequency::code).toList();
  }

  private static BaseCalculationResult resultWith(Notification... notifications) {
    BaseCalculationResult result = new BaseCalculationResult() {};
    result.setWarnings(List.of(notifications));
    return result;
  }

  private static Notification warning(String code) {
    return Notification.builder().code(code).severity(Severity.WARNING).message(code).build();
  }
}
