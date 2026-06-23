package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import static com.fintex.ce.util.DateTimeUtils.getMonthsBetweenDates;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;

public class MaxDrawdownCalculation extends PeriodCalculationAbstract<MaxDrawdownResult, MaxDrawdownEntry> {

  private final NavigableMap<LocalDate, BigDecimal> growth10K;
  private final Function<BigDecimal, BigDecimal> scaleFunction;

  public MaxDrawdownCalculation(final PeriodCalculationInput input,
      final Set<String> defaultPeriods,
      final NavigableMap<LocalDate, BigDecimal> growth10K,
      final Function<BigDecimal, BigDecimal> scaleFunction) {
    super(input, defaultPeriods);
    this.scaleFunction = scaleFunction;
    this.growth10K = growth10K;
  }

  @Override
  public MaxDrawdownEntry calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getPortfolioTotalReturns().size()) {
      return null;
    }
    final NavigableMap<LocalDate, BigDecimal> growth10KByPeriod = new TreeMap<>(
        getSubMapByPeriodStartDate(getPeriodStartDateWithOneMonthOffset(numberOfMonths), growth10K));
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
        scaleFunction.apply(maxDrawdownEntry.getValue()),
        getDrawDownStartDateWithOneMonthOffset(peak),
        maxDrawdownEntry.getKey(),
        recoveryTime);
  }

  @Override
  protected boolean requiresInsufficientDataWarning(final String period, final int availableMonths) {
    if (super.requiresInsufficientDataWarning(period, availableMonths)) {
      return true;
    }
    final int months = getNumberOfMonthsFor(getPortfolioTotalReturns(), period);
    final NavigableMap<LocalDate, BigDecimal> periodGrowth10K = new TreeMap<>(
        getSubMapByPeriodStartDate(getPeriodStartDateWithOneMonthOffset(months), growth10K));
    return calculateMaxDrawdownValues(periodGrowth10K).isEmpty();
  }

  public LocalDate getDrawDownStartDateWithOneMonthOffset(final Map.Entry<LocalDate, BigDecimal> peak) {
    return peak.getKey().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
  }

  public LocalDate getPeriodStartDateWithOneMonthOffset(final int numberOfMonths) {
    return getPeriodStartDate(numberOfMonths, growth10K).minusMonths(1);
  }

  @Override
  public MaxDrawdownResult defineResponseType(final Set<Pair<String, MaxDrawdownEntry>> periodValues) {
    final MaxDrawdownResult result = new MaxDrawdownResult();
    final List<MaxDrawdownEntry> maxDrawdownDtoS = periodValues.stream()
        .map(v -> {
          final MaxDrawdownEntry entry = v.getValue();
          if (entry == null) {
            return new MaxDrawdownEntry(v.getKey(), null, null, null, null);
          }
          return new MaxDrawdownEntry(
              v.getKey(), entry.value(), entry.drawdownStartDate(), entry.drawdownTroughDate(), entry.recoveryTime());
        })
        .toList();
    result.setMaxDrawdown(maxDrawdownDtoS);
    return result;
  }

  /**
   * calculates max drawdown values for each growthOf10K value in period -> ((through value- peak value)/ peak value)
   *
   * @param growth10KByPeriod
   *          growth10K values in period range
   * @return max drawdown values for each month in growthOf10KByPeriod map
   */
  public NavigableMap<LocalDate, BigDecimal> calculateMaxDrawdownValues(
      final NavigableMap<LocalDate, BigDecimal> growth10KByPeriod) {
    final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap = new TreeMap<>();
    growth10KByPeriod.forEach((key, value) -> {
      final NavigableMap<LocalDate, BigDecimal> subMapFromFirstKeyToCustomDate = getSubMapFromFirstKeyToCustomDate(
          growth10KByPeriod, key);
      final BigDecimal maxValue = subMapFromFirstKeyToCustomDate.values().stream().max(Comparator.naturalOrder())
          .orElse(null);
      // Guard the divisor (running peak) before dividing, mirroring the zero-divisor checks in Sharpe/Sortino. A peak
      // of 0 means compounded growth collapsed to 0 (e.g. a -100% month) so the drawdown ratio is undefined; skip the
      // point instead of dividing by zero, which previously surfaced as HTTP 500 (SYS-002).
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

  /**
   * calculates recovery time value. # of months it takes from trough back to a future month when the portfolio value is
   * equal to or greater than the earlier peak value of the max drawdown.
   *
   * @param maximumDrawdownMap
   *          map with max drawdown values
   * @param maxDrawdownEntry
   *          entry with max drawdown month and value
   * @param peak
   *          entry with peak month and value
   * @return # of months
   */
  public Integer getRecoveryTimeValue(final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap,
      final Map.Entry<LocalDate, BigDecimal> maxDrawdownEntry,
      final Map.Entry<LocalDate, BigDecimal> peak) {
    final SortedMap<LocalDate, BigDecimal> mapAfterMaxDrawdown = getSubMapByPeriodStartDate(maxDrawdownEntry.getKey(),
        maximumDrawdownMap);
    final Map.Entry<LocalDate, BigDecimal> recoveryTimeEntry = mapAfterMaxDrawdown.entrySet().stream()
        .filter(e -> e.getValue().compareTo(peak.getValue()) >= 0).findFirst().orElse(null);
    return Objects.nonNull(recoveryTimeEntry)
        ? getMonthsBetweenDates(maxDrawdownEntry.getKey(), recoveryTimeEntry.getKey(), firstDayOfMonth()) - 1
        : null;
  }

  /**
   * returns peak value from first maximumDrawdownMap date to maxDrawdownEntry date
   *
   * @param maximumDrawdownMap
   *          map with max drawdown values
   * @param maxDrawdownEntry
   *          entry with max drawdown month and value
   * @return peak value
   */
  public Map.Entry<LocalDate, BigDecimal> getPeakValue(final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap,
      final Map.Entry<LocalDate, BigDecimal> maxDrawdownEntry) {
    final NavigableMap<LocalDate, BigDecimal> valuesBeforeTheMaxDrawdown = getSubMapFromFirstKeyToCustomDate(
        maximumDrawdownMap, maxDrawdownEntry.getKey());
    return Collections.max(valuesBeforeTheMaxDrawdown.descendingMap().entrySet(), Map.Entry.comparingByValue());
  }

  /**
   * returns max drawdown value from input map
   *
   * @param maximumDrawdownMap
   *          map with max drawdown values
   * @return max drawdown value
   */
  public Map.Entry<LocalDate, BigDecimal> getMaxDrawdownValue(
      final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap) {
    return Collections.min(maximumDrawdownMap.entrySet(), Map.Entry.comparingByValue());
  }

  /**
   * returns sub map from first date in map to custom date
   *
   * @param map
   *          input map that should be reduced
   * @param toDate
   *          custom date
   * @return reduced map
   */
  public NavigableMap<LocalDate, BigDecimal> getSubMapFromFirstKeyToCustomDate(
      final NavigableMap<LocalDate, BigDecimal> map, final LocalDate toDate) {
    return new TreeMap<>(map.subMap(map.firstKey(), true, toDate, true));
  }

}
