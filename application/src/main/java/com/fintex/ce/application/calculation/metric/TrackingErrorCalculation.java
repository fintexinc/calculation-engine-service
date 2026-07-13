package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.BenchmarkWeightedAverageCalculation;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.TrackingErrorResult;
import com.fintex.ce.model.util.BigDecimalConstants;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.application.util.CalculationUtils.sum;
import static com.fintex.ce.application.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.pow;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;

public class TrackingErrorCalculation extends BenchmarkWeightedAverageCalculation<TrackingErrorResult, BigDecimal> {

  public NavigableMap<LocalDate, BigDecimal> portfolioReturnOverBenchmark;

  public TrackingErrorCalculation(final BenchmarkPeriodCalculationInput input,
      final Set<String> periods) {
    super(input, periods);
    portfolioReturnOverBenchmark = calculateExcessPortfolioReturnOverBenchmark();
  }

  @Override
  public TrackingErrorResult defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    TrackingErrorResult result = new TrackingErrorResult();
    Set<TimeIntervalResult> timeIntervals = periodValues.stream().map(e -> new TimeIntervalResult(e.getKey(), e
        .getValue()))
        .collect(Collectors.toSet());
    result.setTrackingError(timeIntervals);
    return result;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getBenchmarkTotalReturns().size()
        || numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths < BigDecimalConstants.TWELVE.intValue()) {
      return null;
    }
    validatePortfolioBenchmarkCoverage(numberOfMonths);
    LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, portfolioReturnOverBenchmark);
    SortedMap<LocalDate, BigDecimal> subMapByPeriodStartDate = getSubMapByPeriodStartDate(periodStartDate,
        portfolioReturnOverBenchmark);
    BigDecimal averageExcessPortfolioReturns = calculateAverageByPeriod(subMapByPeriodStartDate);
    TreeMap<LocalDate, BigDecimal> diff = calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate,
        averageExcessPortfolioReturns);
    return calculateTrackingError(numberOfMonths, diff);
  }

  /**
   * Calculates the tracking error. This formula sums up the values for the timeInterval specified, and divides it by
   * timeIntervalPeriods - 1 . To annualize the value, multiply by SQRT(12)
   *
   * @param diffPortfolioAndAVGPortfolio
   *          final values for calculating tracking errors.
   * @return result of tracking error for period.
   */
  public BigDecimal calculateTrackingError(final int numberOfMonths,
      final TreeMap<LocalDate, BigDecimal> diffPortfolioAndAVGPortfolio) {
    BigDecimal result = divide(sum(diffPortfolioAndAVGPortfolio), BigDecimal.valueOf(numberOfMonths).subtract(
        ONE));
    return toUserScale(DecimalUtils.squareRoot(result).multiply(DecimalUtils.squareRoot(TWELVE)));
  }

  /**
   * Calculates and returns squared difference between each value of subMapByPeriodStartDate and
   * averageExcessPortfolioReturns
   *
   * @param subMapByPeriodStartDate
   *          period since start date
   * @param averageExcessPortfolioReturns
   *          the average of Excess Portfolio Returns during the TimeInterval requested
   * @return squared difference between subMapByPeriodStartDate and averageExcessPortfolioReturns
   */
  public TreeMap<LocalDate, BigDecimal> calculateDiffPortfolioAndAVGPortfolio(
      final SortedMap<LocalDate, BigDecimal> subMapByPeriodStartDate,
      final BigDecimal averageExcessPortfolioReturns) {
    return subMapByPeriodStartDate.entrySet().stream().collect(toTreeMap(
        Map.Entry::getKey, e -> pow(e.getValue().subtract(averageExcessPortfolioReturns), TWO)));
  }

  /**
   * Calculates the average Excess Portfolio Returns during the TimeInterval requested
   *
   * @param subMapByPeriodStartDate
   *          period since start date
   * @return the average of Excess Portfolio Returns during the TimeInterval requested
   */
  public BigDecimal calculateAverageByPeriod(final SortedMap<LocalDate, BigDecimal> subMapByPeriodStartDate) {
    return divide(sum(subMapByPeriodStartDate), BigDecimal.valueOf(subMapByPeriodStartDate.size()));
  }

  /**
   * Calculates the Excess Portfolio Return Over the Benchmark for each month
   * <p>
   * Only dates present in both series are included; requested-period coverage is enforced in
   * {@link #calculatePeriodForNumberOfMonths(int)}.
   *
   * @return map of difference between PortfolioTotalReturns and Benchmark Total Return
   */
  public NavigableMap<LocalDate, BigDecimal> calculateExcessPortfolioReturnOverBenchmark() {
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = getBenchmarkTotalReturns();
    return getPortfolioTotalReturns().entrySet().stream()
        .filter(e -> benchmarkTotalReturns.containsKey(e.getKey()))
        .collect(toTreeMap(Map.Entry::getKey, e -> e.getValue().subtract(benchmarkTotalReturns.get(e.getKey()))));
  }

}
