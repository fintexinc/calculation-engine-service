package com.fintex.ce.adapter.rest.observability;

import java.util.List;

/**
 * Read model of the per-metric calculation statistics: one row per
 * {@link com.fintex.ce.model.domain.enumeration.CalculationMetric} that has been executed at least once, plus an
 * aggregate across all of them. Rows are ordered most-problematic first, so the head of {@code metrics} answers which
 * metrics need attention. Durations describe successful calculations only, so a metric that fails fast does not look
 * fast.
 */
public record CalculationStatisticsReport(Overall overall, List<MetricStatistics> metrics) {

  public record Overall(
      long executions,
      long successes,
      long failures,
      double failureRatePercent,
      long warnings,
      List<CodeFrequency> topErrorCodes,
      List<CodeFrequency> topWarningCodes) {
  }

  public record MetricStatistics(
      String metric,
      long executions,
      long successes,
      long failures,
      double failureRatePercent,
      DurationStatistics duration,
      WarningStatistics warnings,
      List<CodeFrequency> topErrorCodes,
      List<CodeFrequency> topWarningCodes) {
  }

  /**
   * Latency of successful calculations. {@code samples} and {@code meanMillis} are cumulative since startup, while
   * {@code maxMillis} and the percentiles are read from the registry's rolling distribution window and therefore
   * describe recent traffic — a metric idle for a while reports them as zero without losing its sample count.
   */
  public record DurationStatistics(
      long samples,
      double meanMillis,
      double maxMillis,
      double p50Millis,
      double p95Millis,
      double p99Millis) {

    public static final DurationStatistics EMPTY = new DurationStatistics(0, 0, 0, 0, 0, 0);
  }

  public record WarningStatistics(long total, long min, double mean, long max) {

    public static final WarningStatistics EMPTY = new WarningStatistics(0, 0, 0, 0);
  }

  public record CodeFrequency(String code, long count) {
  }
}
