package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.Growth10KHelper;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.fintex.ce.model.domain.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.model.domain.enumeration.Period.SINCE_PERFORMANCE_START_DATE;
import static com.fintex.ce.model.domain.enumeration.Period.YEAR_TO_DATE;
import static com.fintex.ce.util.DateTimeUtils.getMonthsBetweenDates;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Service
public class MaxDrawdownService extends WeightedAverageWithCpedAbstractService<PeriodCommand, MaxDrawdownResult> {

  public MaxDrawdownService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      @Value("${default.periods.risk-calculations}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MAX_DRAWDOWN;
  }

  @Override
  public MaxDrawdownResult perform(final PeriodCommand command) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    final NavigableMap<LocalDate, BigDecimal> portfolioReturns = context.getWeightedAveragePortfolioReturns();
    final LocalDate cipsd = context.getCipsd();
    // portfolioReturns is already in factor form, pass AS_IS to avoid double-scaling
    final NavigableMap<LocalDate, BigDecimal> growth10K = Growth10KHelper.compoundGrowth10K(
        portfolioReturns, ReturnFactorScale.AS_IS);

    final Set<String> initialPeriods = CollectionUtils.isEmpty(command.getPeriods())
        ? defaultPeriods
        : command.getPeriods();
    final Set<Pair<String, MaxDrawdownEntry>> periodsResult = new HashSet<>();

    initialPeriods.stream()
        .filter(p -> !SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name().equalsIgnoreCase(p))
        .forEach(p -> {
          final int months = getNumberOfMonthsFor(portfolioReturns, p.trim());
          periodsResult.add(Pair.of(p, calculateEntry(months, portfolioReturns, growth10K)));
        });

    if (isCipsdValid(cipsd, portfolioReturns)) {
      final int months = getMonthsBetweenDates(cipsd, portfolioReturns.lastKey(), firstDayOfMonth());
      periodsResult.add(Pair.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(),
          calculateEntry(months, portfolioReturns, growth10K)));
    } else if (cipsd != null || initialPeriods.contains(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name())) {
      periodsResult.add(Pair.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), null));
    }

    final MaxDrawdownResult result = buildResult(periodsResult);
    result.setCustomIntervalPerformanceStartDate(cipsd);
    result.setPerformanceEndDate(portfolioReturns.lastKey());
    result.setPerformanceStartDate(portfolioReturns.firstKey());
    addWarnings(result, periodsResult, portfolioReturns, cipsd, growth10K);
    return result;
  }

  // Package-private for use by MarRatioCalculationService (same package) and for unit testing.

  MaxDrawdownEntry calculateEntry(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final NavigableMap<LocalDate, BigDecimal> growth10K) {
    if (numberOfMonths > portfolioReturns.size()) {
      return null;
    }
    final NavigableMap<LocalDate, BigDecimal> growth10KByPeriod = new TreeMap<>(
        getSubMapByPeriodStartDate(getPeriodStartDateWithOneMonthOffset(numberOfMonths, growth10K), growth10K));
    final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap = calculateMaxDrawdownValues(growth10KByPeriod);

    if (maximumDrawdownMap.isEmpty()) {
      return null;
    }
    final Map.Entry<LocalDate, BigDecimal> maxDrawdownEntry = getMaxDrawdownValue(maximumDrawdownMap);
    if (maxDrawdownEntry.getValue().compareTo(BigDecimal.ZERO) == 0) {
      return new MaxDrawdownEntry(String.valueOf(numberOfMonths), BigDecimal.ZERO, null, null, null);
    }
    final Map.Entry<LocalDate, BigDecimal> peak = getPeakValue(maximumDrawdownMap, maxDrawdownEntry);
    final Integer recoveryTime = getRecoveryTimeValue(maximumDrawdownMap, maxDrawdownEntry, peak);
    return new MaxDrawdownEntry(
        String.valueOf(numberOfMonths),
        maxDrawdownEntry.getValue(),
        getDrawDownStartDateWithOneMonthOffset(peak),
        maxDrawdownEntry.getKey(),
        recoveryTime);
  }

  NavigableMap<LocalDate, BigDecimal> calculateMaxDrawdownValues(
      final NavigableMap<LocalDate, BigDecimal> growth10KByPeriod) {
    final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap = new TreeMap<>();
    growth10KByPeriod.forEach((key, value) -> {
      final NavigableMap<LocalDate, BigDecimal> subMap = getSubMapFromFirstKeyToCustomDate(growth10KByPeriod, key);
      final BigDecimal maxValue = subMap.values().stream().max(Comparator.naturalOrder()).orElse(null);
      // Guard the divisor (running peak) before dividing. A peak of 0 means compounded growth collapsed to 0
      // (e.g. a -100% month) so the drawdown ratio is undefined; skip rather than divide by zero.
      if (maxValue == null || maxValue.compareTo(BigDecimal.ZERO) == 0) {
        return;
      }
      final BigDecimal maxDrawdownValue = DecimalUtils.divide(value.subtract(maxValue), maxValue);
      maximumDrawdownMap.put(key, maxDrawdownValue.compareTo(BigDecimal.ZERO) <= 0
          ? maxDrawdownValue
          : BigDecimal.ZERO);
    });
    return maximumDrawdownMap;
  }

  Map.Entry<LocalDate, BigDecimal> getMaxDrawdownValue(
      final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap) {
    return Collections.min(maximumDrawdownMap.entrySet(), Map.Entry.comparingByValue());
  }

  Map.Entry<LocalDate, BigDecimal> getPeakValue(final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap,
      final Map.Entry<LocalDate, BigDecimal> maxDrawdownEntry) {
    final NavigableMap<LocalDate, BigDecimal> valuesBeforeMaxDrawdown = getSubMapFromFirstKeyToCustomDate(
        maximumDrawdownMap, maxDrawdownEntry.getKey());
    return Collections.max(valuesBeforeMaxDrawdown.descendingMap().entrySet(), Map.Entry.comparingByValue());
  }

  Integer getRecoveryTimeValue(final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap,
      final Map.Entry<LocalDate, BigDecimal> maxDrawdownEntry,
      final Map.Entry<LocalDate, BigDecimal> peak) {
    final SortedMap<LocalDate, BigDecimal> mapAfterMaxDrawdown = getSubMapByPeriodStartDate(
        maxDrawdownEntry.getKey(), maximumDrawdownMap);
    final Map.Entry<LocalDate, BigDecimal> recoveryTimeEntry = mapAfterMaxDrawdown.entrySet().stream()
        .filter(e -> e.getValue().compareTo(peak.getValue()) >= 0).findFirst().orElse(null);
    return Objects.nonNull(recoveryTimeEntry)
        ? getMonthsBetweenDates(maxDrawdownEntry.getKey(), recoveryTimeEntry.getKey(), firstDayOfMonth()) - 1
        : null;
  }

  LocalDate getDrawDownStartDateWithOneMonthOffset(final Map.Entry<LocalDate, BigDecimal> peak) {
    return peak.getKey().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
  }

  LocalDate getPeriodStartDateWithOneMonthOffset(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> growth10K) {
    return getPeriodStartDate(numberOfMonths, growth10K).minusMonths(1);
  }

  NavigableMap<LocalDate, BigDecimal> getSubMapFromFirstKeyToCustomDate(
      final NavigableMap<LocalDate, BigDecimal> map, final LocalDate toDate) {
    return new TreeMap<>(map.subMap(map.firstKey(), true, toDate, true));
  }

  LocalDate getPeriodStartDate(final int numberOfMonths, final SortedMap<LocalDate, BigDecimal> returns) {
    return toLastDayOfMonth(returns.lastKey().minusMonths((long) numberOfMonths - 1));
  }

  SortedMap<LocalDate, BigDecimal> getSubMapByPeriodStartDate(final LocalDate periodStartDate,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    return returns.subMap(periodStartDate, true, returns.lastKey(), true);
  }

  MaxDrawdownResult buildResult(final Set<Pair<String, MaxDrawdownEntry>> periodsResult) {
    final MaxDrawdownResult result = new MaxDrawdownResult();
    final List<MaxDrawdownEntry> entries = periodsResult.stream()
        .map(v -> {
          final MaxDrawdownEntry entry = v.getValue();
          if (entry == null) {
            return new MaxDrawdownEntry(v.getKey(), null, null, null, null);
          }
          return new MaxDrawdownEntry(
              v.getKey(),
              entry.value() != null ? DecimalUtils.toUserScale(entry.value()) : null,
              entry.drawdownStartDate(),
              entry.drawdownTroughDate(),
              entry.recoveryTime());
        })
        .toList();
    result.setMaxDrawdown(entries);
    return result;
  }

  // Private helpers

  private void addWarnings(final MaxDrawdownResult result,
      final Set<Pair<String, MaxDrawdownEntry>> periodsResult,
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final LocalDate cipsd,
      final NavigableMap<LocalDate, BigDecimal> growth10K) {
    final int availableMonths = portfolioReturns.size();
    final List<Notification> warnings = new ArrayList<>(result.getWarnings());

    periodsResult.stream()
        .filter(pair -> pair.getValue() == null)
        .filter(pair -> !SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name().equalsIgnoreCase(pair.getKey().trim()))
        .filter(pair -> getNumberOfMonthsFor(portfolioReturns, pair.getKey().trim()) > availableMonths)
        .map(pair -> ErrorCode.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD.asNotification(pair.getKey().trim(),
            availableMonths))
        .forEach(warnings::add);

    periodsResult.stream()
        .filter(pair -> pair.getValue() == null)
        .filter(pair -> !SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name().equalsIgnoreCase(pair.getKey().trim()))
        .filter(pair -> getNumberOfMonthsFor(portfolioReturns, pair.getKey().trim()) <= availableMonths)
        .filter(pair -> isDegenerateGrowthData(pair.getKey().trim(), portfolioReturns, growth10K))
        .map(pair -> ErrorCode.DEGENERATE_GROWTH_DATA.asNotification(pair.getKey().trim()))
        .forEach(warnings::add);

    final boolean sinceCipsdRequestedAndNull = periodsResult.stream()
        .anyMatch(pair -> SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()
            .equalsIgnoreCase(pair.getKey().trim()) && pair.getValue() == null);
    if (cipsd != null && !portfolioReturns.isEmpty() && !isCipsdValid(cipsd, portfolioReturns)
        && sinceCipsdRequestedAndNull) {
      warnings.add(ErrorCode.CIPSD_OUTSIDE_DATA_RANGE.asNotification(
          cipsd, portfolioReturns.firstKey(), portfolioReturns.lastKey()));
    }

    result.setWarnings(warnings);
  }

  private boolean isDegenerateGrowthData(final String period,
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final NavigableMap<LocalDate, BigDecimal> growth10K) {
    final int months = getNumberOfMonthsFor(portfolioReturns, period);
    final SortedMap<LocalDate, BigDecimal> periodGrowth10K = getSubMapByPeriodStartDate(
        getPeriodStartDateWithOneMonthOffset(months, growth10K), growth10K);
    return !periodGrowth10K.isEmpty()
        && periodGrowth10K.get(periodGrowth10K.firstKey()).compareTo(BigDecimal.ZERO) == 0;
  }

  private int getNumberOfMonthsFor(final NavigableMap<LocalDate, BigDecimal> returns, final String period) {
    if (isNumeric(period)) {
      return Integer.parseInt(period);
    } else if (YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
      final LocalDate endDate = returns.keySet().stream().max(LocalDate::compareTo).orElseThrow();
      return getMonthsBetweenDates(endDate, endDate, firstDayOfYear());
    } else if (SINCE_PERFORMANCE_START_DATE.name().equalsIgnoreCase(period)) {
      return getMonthsBetweenDates(returns.firstKey(), returns.lastKey(), firstDayOfMonth());
    }
    throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_ALLOWED.toException(period);
  }

  private boolean isCipsdValid(final LocalDate cipsd, final NavigableMap<LocalDate, BigDecimal> returns) {
    return cipsd != null
        && returns.firstKey().compareTo(cipsd) <= 0
        && returns.lastKey().compareTo(cipsd) >= 0;
  }
}
