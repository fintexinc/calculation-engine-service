package com.fintex.ce.adapter.observability.calculation;

import com.fintex.ce.port.observability.CalculationStatisticsProvider;
import com.fintex.ce.port.observability.CalculationStatisticsReport;
import com.fintex.ce.port.observability.CalculationStatisticsReport.CodeFrequency;
import com.fintex.ce.port.observability.CalculationStatisticsReport.DurationStatistics;
import com.fintex.ce.port.observability.CalculationStatisticsReport.MetricStatistics;
import com.fintex.ce.port.observability.CalculationStatisticsReport.Overall;
import com.fintex.ce.port.observability.CalculationStatisticsReport.WarningStatistics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Turns the per-metric calculation meters back into a ranked summary, so the most problematic calculation metrics can
 * be seen without an external metrics backend. Rows are ordered by absolute failure count first and failure ratio
 * second, so a metric that fails often on real traffic outranks one that failed its only call.
 *
 * <p>
 * The registry is the store: nothing is accumulated here beyond the lifetime of one call. That is what keeps this
 * consistent with whatever the meters are exported to — there is no second tally that could drift from the first — and
 * it is why the numbers are cumulative since startup and lost on restart.
 */
@Component
public class MeterCalculationStatisticsProvider implements CalculationStatisticsProvider {

  private static final int TOP_CODES = 5;
  private static final double TOLERANCE = 1e-9;

  private final MeterRegistry meterRegistry;

  public MeterCalculationStatisticsProvider(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public CalculationStatisticsReport statistics() {
    Map<String, Accumulator> byMetric = new TreeMap<>();

    meterRegistry.find(CalculationMetricStatistics.EXECUTIONS_METER_NAME).counters()
        .forEach(counter -> accumulateExecution(byMetric, counter));
    meterRegistry.find(CalculationMetricStatistics.DURATION_METER_NAME).timers()
        .forEach(timer -> accumulateDuration(byMetric, timer));
    meterRegistry.find(CalculationMetricStatistics.WARNINGS_METER_NAME).summaries()
        .forEach(summary -> accumulateWarnings(byMetric, summary));
    meterRegistry.find(CalculationMetricStatistics.WARNINGS_MIN_METER_NAME).gauges()
        .forEach(gauge -> accumulateWarningsMin(byMetric, gauge));
    meterRegistry.find(CalculationMetricStatistics.ERROR_CODES_METER_NAME).counters()
        .forEach(counter -> accumulateCode(byMetric, counter,
            CalculationMetricStatistics.ERROR_CODE_TAG, accumulator -> accumulator.errorCodes));
    meterRegistry.find(CalculationMetricStatistics.WARNING_CODES_METER_NAME).counters()
        .forEach(counter -> accumulateCode(byMetric, counter,
            CalculationMetricStatistics.WARNING_CODE_TAG, accumulator -> accumulator.warningCodes));

    List<MetricStatistics> metrics = byMetric.entrySet().stream()
        .map(entry -> entry.getValue().toStatistics(entry.getKey()))
        .sorted(Comparator.comparingLong(MetricStatistics::failures).reversed()
            .thenComparing(Comparator.comparingDouble(MetricStatistics::failureRatePercent).reversed())
            .thenComparing(MetricStatistics::metric))
        .toList();

    return new CalculationStatisticsReport(overall(metrics, byMetric.values()), metrics);
  }

  private static void accumulateExecution(Map<String, Accumulator> byMetric, Counter counter) {
    Accumulator accumulator = accumulatorFor(byMetric, counter);
    if (accumulator == null) {
      return;
    }
    long count = (long) counter.count();
    if (isSuccess(counter)) {
      accumulator.successes += count;
    } else {
      accumulator.failures += count;
    }
  }

  private static void accumulateDuration(Map<String, Accumulator> byMetric, Timer timer) {
    Accumulator accumulator = accumulatorFor(byMetric, timer);
    if (accumulator == null || !isSuccess(timer) || timer.count() == 0) {
      return;
    }
    HistogramSnapshot snapshot = timer.takeSnapshot();
    accumulator.duration = new DurationStatistics(
        timer.count(),
        round(timer.mean(TimeUnit.MILLISECONDS)),
        round(timer.max(TimeUnit.MILLISECONDS)),
        round(percentile(snapshot, 0.5)),
        round(percentile(snapshot, 0.95)),
        round(percentile(snapshot, 0.99)));
  }

  private static void accumulateWarnings(Map<String, Accumulator> byMetric, DistributionSummary summary) {
    Accumulator accumulator = accumulatorFor(byMetric, summary);
    if (accumulator == null) {
      return;
    }
    accumulator.warningsTotal += (long) summary.totalAmount();
    accumulator.warningsSamples += summary.count();
    accumulator.warningsMax = Math.max(accumulator.warningsMax, (long) summary.max());
  }

  private static void accumulateWarningsMin(Map<String, Accumulator> byMetric, Gauge gauge) {
    Accumulator accumulator = accumulatorFor(byMetric, gauge);
    if (accumulator != null) {
      accumulator.warningsMin = (long) gauge.value();
      accumulator.warningsMinKnown = true;
    }
  }

  private static void accumulateCode(
      Map<String, Accumulator> byMetric,
      Counter counter,
      String codeTag,
      Function<Accumulator, Map<String, Long>> target) {
    Accumulator accumulator = accumulatorFor(byMetric, counter);
    String code = tag(counter, codeTag);
    if (accumulator == null || code == null) {
      return;
    }
    target.apply(accumulator).merge(code, (long) counter.count(), Long::sum);
  }

  private static Accumulator accumulatorFor(Map<String, Accumulator> byMetric, Meter meter) {
    String metric = tag(meter, CalculationMetricStatistics.METRIC_TAG);
    return metric == null ? null : byMetric.computeIfAbsent(metric, key -> new Accumulator());
  }

  private static boolean isSuccess(Meter meter) {
    return CalculationMetricStatistics.SUCCESS.equals(tag(meter, CalculationMetricStatistics.OUTCOME_TAG));
  }

  private static String tag(Meter meter, String key) {
    return meter.getId().getTag(key);
  }

  private static double percentile(HistogramSnapshot snapshot, double target) {
    return Arrays.stream(snapshot.percentileValues())
        .filter(value -> Math.abs(value.percentile() - target) < TOLERANCE)
        .mapToDouble(value -> value.value(TimeUnit.MILLISECONDS))
        .findFirst()
        .orElse(0);
  }

  /**
   * The overall code rankings are merged from every accumulator's full tally, not from the per-metric top lists: a code
   * that ranks below the cut-off for each metric individually can still be the most frequent code overall, and merging
   * the truncated lists would drop it entirely. Truncation happens once, after merging.
   */
  private static Overall overall(List<MetricStatistics> metrics, Collection<Accumulator> accumulators) {
    long successes = metrics.stream().mapToLong(MetricStatistics::successes).sum();
    long failures = metrics.stream().mapToLong(MetricStatistics::failures).sum();
    long warnings = metrics.stream().mapToLong(statistics -> statistics.warnings().total()).sum();
    return new Overall(
        successes + failures,
        successes,
        failures,
        percentage(failures, successes + failures),
        warnings,
        mergeTop(accumulators, accumulator -> accumulator.errorCodes),
        mergeTop(accumulators, accumulator -> accumulator.warningCodes));
  }

  private static List<CodeFrequency> mergeTop(
      Collection<Accumulator> accumulators,
      Function<Accumulator, Map<String, Long>> extractor) {
    Map<String, Long> merged = new HashMap<>();
    accumulators.forEach(accumulator -> extractor.apply(accumulator)
        .forEach((code, count) -> merged.merge(code, count, Long::sum)));
    return topCodes(merged);
  }

  private static List<CodeFrequency> topCodes(Map<String, Long> counts) {
    return counts.entrySet().stream()
        .map(entry -> new CodeFrequency(entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparingLong(CodeFrequency::count).reversed()
            .thenComparing(CodeFrequency::code))
        .limit(TOP_CODES)
        .toList();
  }

  private static double percentage(long part, long total) {
    return total == 0 ? 0 : round(part * 100.0 / total);
  }

  private static double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private static final class Accumulator {

    private final Map<String, Long> errorCodes = new HashMap<>();
    private final Map<String, Long> warningCodes = new HashMap<>();

    private long successes;
    private long failures;
    private long warningsTotal;
    private long warningsSamples;
    private long warningsMax;
    private long warningsMin;
    private boolean warningsMinKnown;
    private DurationStatistics duration = DurationStatistics.EMPTY;

    private MetricStatistics toStatistics(String metric) {
      long executions = successes + failures;
      WarningStatistics warnings = warningsSamples == 0
          ? WarningStatistics.EMPTY
          : new WarningStatistics(
              warningsTotal,
              warningsMinKnown ? warningsMin : 0,
              round(warningsTotal * 1.0 / warningsSamples),
              warningsMax);
      return new MetricStatistics(
          metric,
          executions,
          successes,
          failures,
          percentage(failures, executions),
          duration,
          warnings,
          topCodes(errorCodes),
          topCodes(warningCodes));
    }
  }
}
