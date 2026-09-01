package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.result.period.BestWorstPeriodData;
import com.fintex.ce.model.domain.result.period.BestWorstPeriodsResult;
import com.fintex.ce.model.domain.result.period.IntervalResult;
import com.fintex.ce.model.domain.result.period.PeriodDateResult;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ZERO;

@EqualsAndHashCode
public class BestWorstPeriodCalculation {

  private static final int ONE = 1;
  private static final int TWELVE = 12;

  private final Set<Long> periods;
  public BestWorstPeriodData bestWorstPeriodData = new BestWorstPeriodData();

  private final NavigableMap<LocalDate, BigDecimal> portfolioReturns;

  public BestWorstPeriodCalculation(final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final Set<Long> periods) {
    this.portfolioReturns = portfolioReturns;
    this.periods = periods;
  }

  public BestWorstPeriodsResult calculate() {
    if (CollectionUtils.isEmpty(portfolioReturns)) {
      return null;
    }
    for (final Long period : periods) {
      final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns = calculateMonthRollingCumulativeReturns(period);
      calculateBestWorstPeriodValues(period, rollingCumulativeReturns);
    }
    return BestWorstPeriodsResult.builder()
        .performanceEndDate(portfolioReturns.lastKey())
        .performanceStartDate(portfolioReturns.firstKey())
        .bestWorstPeriods(bestWorstPeriodData)
        .build();
  }

  public void calculateBestWorstPeriodValues(final Long period,
      final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns) {
    final LocalDate startOfPeriods = getStartOfPeriodsDate(period, rollingCumulativeReturns);
    if (startOfPeriods.compareTo(rollingCumulativeReturns.lastKey()) <= 0) {
      final TreeMap<LocalDate, BigDecimal> subMapByStartPeriodDate = getMapByPeriodStartDate(rollingCumulativeReturns,
          startOfPeriods);
      calculateNumberOfPeriods(period, subMapByStartPeriodDate);
      calculateAverage(period, subMapByStartPeriodDate);
      calculateBestPeriodValue(period, subMapByStartPeriodDate);
      calculateWorstPeriodValue(period, subMapByStartPeriodDate);
      calculatePositive(period, subMapByStartPeriodDate);
    } else {
      addDefaultValues(period);
    }
  }

  public void addDefaultValues(final Long period) {
    bestWorstPeriodData.getNumberOfPeriods().put(period, ZERO);
    bestWorstPeriodData.getAverage().put(period, null);
    bestWorstPeriodData.getBestPeriodPct().put(period, null);
    bestWorstPeriodData.getBestPeriodDate().add(new PeriodDateResult(period, null));
    bestWorstPeriodData.getWorstPeriodPct().put(period, null);
    bestWorstPeriodData.getWorstPeriodDate().add(new PeriodDateResult(period, null));
    bestWorstPeriodData.getPctPositive().put(period, null);
  }

  public void calculateNumberOfPeriods(final Long period,
      final TreeMap<LocalDate, BigDecimal> subMapRollingCumulativeReturns) {
    final int numOfPeriods = subMapRollingCumulativeReturns.size();
    bestWorstPeriodData.getNumberOfPeriods().put(period, BigDecimal.valueOf(numOfPeriods));
  }

  public void calculatePositive(final Long period,
      final TreeMap<LocalDate, BigDecimal> subMapRollingCumulativeReturns) {
    final long numberOfPositiveValues = subMapRollingCumulativeReturns.values().stream().filter(v -> v.compareTo(
        ZERO) > 0).count();
    final BigDecimal positive = DecimalUtils.divide(BigDecimal.valueOf(numberOfPositiveValues),
        getNumberOfPeriodsByPeriod(period));
    bestWorstPeriodData.getPctPositive().put(period, DecimalUtils.toUserScale(positive));
  }

  public void calculateAverage(final Long period, final TreeMap<LocalDate, BigDecimal> subMapRollingCumulativeReturns) {
    final BigDecimal rollingComulativeReturtsSum = subMapRollingCumulativeReturns.values().stream().reduce(ZERO,
        BigDecimal::add);
    final BigDecimal average = DecimalUtils.divide(rollingComulativeReturtsSum, subMapRollingCumulativeReturns.size());
    bestWorstPeriodData.getAverage().put(period, annualize(average, period));
  }

  public void calculateBestPeriodValue(final Long period,
      final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns) {
    final LocalDate bestPeriodDate = rollingCumulativeReturns.entrySet().stream().sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey).toList().get(rollingCumulativeReturns.size() - ONE);
    bestWorstPeriodData.getBestPeriodPct().put(period, annualize(rollingCumulativeReturns.get(bestPeriodDate), period));
    bestWorstPeriodData.getBestPeriodDate().add(new PeriodDateResult(period, new IntervalResult(getStartDate(period,
        bestPeriodDate), bestPeriodDate)));
  }

  public void calculateWorstPeriodValue(final Long period,
      final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns) {
    final LocalDate worstPeriodDate = rollingCumulativeReturns.entrySet().stream().sorted(Map.Entry
        .<LocalDate, BigDecimal>comparingByValue().reversed()).map(Map.Entry::getKey).toList().get(
            rollingCumulativeReturns.size() - ONE);
    bestWorstPeriodData.getWorstPeriodPct().put(period,
        annualize(rollingCumulativeReturns.get(worstPeriodDate), period));
    bestWorstPeriodData.getWorstPeriodDate().add(new PeriodDateResult(period, new IntervalResult(getStartDate(period,
        worstPeriodDate), worstPeriodDate)));
  }

  private LocalDate getStartDate(final Long period, final LocalDate bestPeriodDate) {
    return bestPeriodDate.minusMonths(period - ONE).with(TemporalAdjusters.firstDayOfMonth());
  }

  /**
   * returns annualized value by period. If period >= 12 returns (value+1)^(12/period) . If period < 12 returns
   * value.</>
   *
   * @param value
   * @param period
   * @return
   */
  public BigDecimal annualize(final BigDecimal value, final Long period) {
    if (period >= TWELVE) {
      return DecimalUtils.toUserScale(DecimalUtils.pow(BigDecimal.ONE.add(value), DecimalUtils.divide(new BigDecimal(
          TWELVE), period)).subtract(BigDecimal.ONE));
    } else {
      return DecimalUtils.toUserScale(value);
    }
  }

  /**
   * returns rolling cumulative returns calculated by period
   *
   * @param period
   *          - number of months
   * @return rolling cumulative returns
   */
  public TreeMap<LocalDate, BigDecimal> calculateMonthRollingCumulativeReturns(final Long period) {
    final TreeMap<LocalDate, BigDecimal> cumulativeReturns = new TreeMap<>();
    portfolioReturns.forEach((key, value) -> {
      final SortedMap<LocalDate, BigDecimal> periodDates = portfolioReturns.subMap(key.minusMonths(period - ONE), true,
          key, true);
      final BigDecimal cumulativeReturn = DecimalUtils.toUserScale(periodDates.values().stream().reduce(BigDecimal.ONE,
          BigDecimal::multiply).subtract(BigDecimal.ONE));
      cumulativeReturns.put(key, cumulativeReturn);
    });
    return cumulativeReturns;
  }

  /**
   * returns number of periods by period value from bestWorstPeriodData
   *
   * @param period
   *          - number of months
   * @return number of periods value for the current period
   */
  public BigDecimal getNumberOfPeriodsByPeriod(final Long period) {
    return bestWorstPeriodData.getNumberOfPeriods().get(period);
  }

  /**
   * returns period start date by number of months. Last date in return factor list minus numberOfMonths-1
   *
   * @param period
   *          number of month in period
   * @param rollingCumulativeReturns
   *          rolling cumulative returns
   * @return period start date
   */
  public LocalDate getStartOfPeriodsDate(final Long period,
      final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns) {
    return toLastDayOfMonth(rollingCumulativeReturns.firstKey().plusMonths(period - ONE));
  }

  /**
   * Returns sub map from startOfPeriodsDate to the end of the rollingCumulativeReturns
   *
   * @param rollingCumulativeReturns
   *          rolling cumulative returns
   * @param startOfPeriods
   *          date when periods start
   * @return calculated map
   */
  public TreeMap<LocalDate, BigDecimal> getMapByPeriodStartDate(
      final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns, final LocalDate startOfPeriods) {
    return new TreeMap<>(rollingCumulativeReturns.subMap(startOfPeriods, true, rollingCumulativeReturns.lastKey(),
        true));
  }

}
