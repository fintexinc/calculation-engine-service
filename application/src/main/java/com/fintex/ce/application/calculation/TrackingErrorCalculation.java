package com.fintex.ce.application.calculation;

import com.fintex.ce.domain.constant.BigDecimalConstants;
import com.fintex.ce.application.calculation.core.BenchmarkWeightedAverageCalculation;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.model.result.TrackingErrorResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import com.fintex.ce.util.DecimalUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.domain.constant.BigDecimalConstants.*;
import static com.fintex.ce.util.CalculationUtils.sum;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DecimalUtils.*;
import static java.util.Optional.ofNullable;

public class TrackingErrorCalculation extends BenchmarkWeightedAverageCalculation<TrackingErrorResult, BigDecimal> {

  public NavigableMap<LocalDate, BigDecimal> portfolioReturnOverBenchmark;

  public TrackingErrorCalculation(final BenchmarkCalculationDTO input,
      final Set<String> periods) {
    super(input, periods);
    portfolioReturnOverBenchmark = calculateExcessPortfolioReturnOverBenchmark();
  }

  @Override
  public TrackingErrorResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final TrackingErrorResult resDto = new TrackingErrorResult();
    final Set<TimeIntervalResult> timeIntervals = result.stream().map(e -> new TimeIntervalResult(e.getKey(), e
        .getValue()))
        .collect(Collectors.toSet());
    resDto.setTrackingError(timeIntervals);
    return resDto;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getBenchmarkTotalReturns().size()
        || numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths < BigDecimalConstants.TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, portfolioReturnOverBenchmark);
    final SortedMap<LocalDate, BigDecimal> subMapByPeriodStartDate = getSubMapByPeriodStartDate(periodStartDate,
        portfolioReturnOverBenchmark);
    final BigDecimal averageExcessPortfolioReturns = calculateAverageByPeriod(subMapByPeriodStartDate);
    final TreeMap<LocalDate, BigDecimal> diff = calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate,
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
    final BigDecimal result = divide(sum(diffPortfolioAndAVGPortfolio), BigDecimal.valueOf(numberOfMonths).subtract(
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
   *
   * When getPortfolioTotalReturns() consists of 180 entries and getBenchmarkTotalReturns() of 100 entries Then this
   * method would return 180 entries portfolioReturnOverBenchmark But 1..100 entries would ok and 101-180 entries would
   * be incorrect. This was done by purpose. Previously we make benchmarkTotalReturn and portfolioTotalReturn the same
   * size. But now they could be of different sizes. Later in execution in calculatePeriodForNumberOfMonths there is
   * check with if statement which would not allow period greater than 100 to access 101-180 entries of
   * portfolioReturnOverBenchmark
   *
   * @return map of difference between PortfolioTotalReturns and Benchmark Total Return
   */
  public NavigableMap<LocalDate, BigDecimal> calculateExcessPortfolioReturnOverBenchmark() {
    return getPortfolioTotalReturns().entrySet().stream().collect(toTreeMap(
        Map.Entry::getKey, e -> e.getValue().subtract(ofNullable(getBenchmarkTotalReturns().get(e.getKey())).orElse(
            BigDecimal.ZERO))));
  }

}
