package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.result.MaxDrawdownResult;
import com.fintex.ce.application.result.core.MaxDrawdownEntry;
import com.fintex.ce.util.DecimalUtils;
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
import java.util.stream.Collectors;

import static com.fintex.ce.util.DateTimeUtils.getMonthsBetweenDates;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.util.Optional.ofNullable;

public class MaxDrawdownCalculation extends PeriodCalculationAbstract<MaxDrawdownResult, MaxDrawdownEntry> {

  private final NavigableMap<LocalDate, BigDecimal> growth10K;
  private final Function<BigDecimal, BigDecimal> scaleFunction;

  public MaxDrawdownCalculation(final CalculationDTO input,
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
      return new MaxDrawdownEntry().setTimeIntervalPeriod(String.valueOf(numberOfMonths));
    }
    final NavigableMap<LocalDate, BigDecimal> growth10KByPeriod = new TreeMap<>(
        getSubMapByPeriodStartDate(getPeriodStartDateWithOneMonthOffset(numberOfMonths), growth10K));
    final NavigableMap<LocalDate, BigDecimal> maximumDrawdownMap = calculateMaxDrawdownValues(growth10KByPeriod);
    final Map.Entry<LocalDate, BigDecimal> maxDrawdownEntry = getMaxDrawdownValue(maximumDrawdownMap);
    if (maxDrawdownEntry.getValue().compareTo(BigDecimal.ZERO) == 0) {
      return new MaxDrawdownEntry().setValue(BigDecimal.ZERO).setTimeIntervalPeriod(String.valueOf(numberOfMonths));
    }
    final Map.Entry<LocalDate, BigDecimal> peak = getPeakValue(maximumDrawdownMap, maxDrawdownEntry);
    final Integer recoveryTime = getRecoveryTimeValue(maximumDrawdownMap, maxDrawdownEntry, peak);
    return new MaxDrawdownEntry()
        .setTimeIntervalPeriod(String.valueOf(numberOfMonths))
        .setValue(scaleFunction.apply(maxDrawdownEntry.getValue()))
        .setDrawdownStartDate(getDrawDownStartDateWithOneMonthOffset(peak))
        .setDrawdownTroughDate(maxDrawdownEntry.getKey()).setRecoveryTime(recoveryTime);
  }

  public LocalDate getDrawDownStartDateWithOneMonthOffset(final Map.Entry<LocalDate, BigDecimal> peak) {
    return peak.getKey().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
  }

  public LocalDate getPeriodStartDateWithOneMonthOffset(final int numberOfMonths) {
    return getPeriodStartDate(numberOfMonths, growth10K).minusMonths(1);
  }

  @Override
  public MaxDrawdownResult defineResponseType(final Set<Pair<String, MaxDrawdownEntry>> result) {
    final MaxDrawdownResult maxDrawdownResDTO = new MaxDrawdownResult();
    final List<MaxDrawdownEntry> maxDrawdownDTOS = result.stream()
        .map(v -> ofNullable(v.getValue()).orElse(new MaxDrawdownEntry()).setTimeIntervalPeriod(v.getKey()))
        .collect(Collectors.toList());
    maxDrawdownResDTO.setMaxDrawdown(maxDrawdownDTOS);
    return maxDrawdownResDTO;
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
