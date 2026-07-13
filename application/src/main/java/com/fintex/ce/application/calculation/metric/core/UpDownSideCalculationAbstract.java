package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.PeriodResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * This abstraction is applicable for Upside and Downside calculations.
 *
 * @param <T>
 *          response type
 */
public abstract class UpDownSideCalculationAbstract<T extends PeriodResult>
    extends
      BenchmarkWeightedAverageCalculation<T, BigDecimal> {

  public NavigableMap<LocalDate, BigDecimal> portfolioDetermination;
  public NavigableMap<LocalDate, BigDecimal> benchmarkDetermination;

  protected UpDownSideCalculationAbstract(BenchmarkPeriodCalculationInput input, Set<String> periods) {
    super(input, periods);
    portfolioDetermination = getPortfolioDetermination();
    benchmarkDetermination = getBenchmarkDetermination(portfolioDetermination);
  }

  public abstract boolean filterCaptureExpression(final Map.Entry<LocalDate, BigDecimal> e);

  public TreeMap<LocalDate, BigDecimal> getPortfolioDetermination() {
    NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = getPortfolioTotalReturns();
    return getBenchmarkTotalReturns().entrySet().stream().filter(this::filterCaptureExpression)
        .filter(e -> portfolioTotalReturns.containsKey(e.getKey()))
        .collect(toTreeMap(Map.Entry::getKey,
            e -> divide(portfolioTotalReturns.get(e.getKey()), HUNDRED).add(ONE)));
  }

  public NavigableMap<LocalDate, BigDecimal> getBenchmarkDetermination(
      final NavigableMap<LocalDate, BigDecimal> portfolioDownsideDetermination) {
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = getBenchmarkTotalReturns();
    return portfolioDownsideDetermination.entrySet().stream()
        .filter(e -> benchmarkTotalReturns.containsKey(e.getKey()))
        .collect(toTreeMap(Map.Entry::getKey,
            e -> divide(benchmarkTotalReturns.get(e.getKey()), HUNDRED).add(ONE)));
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getBenchmarkTotalReturns().size()
        || numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths < 12) {
      return null;
    }
    validatePortfolioBenchmarkCoverage(numberOfMonths);
    BigDecimal portfolioCaptureReturn = calculateCaptureReturnFor(numberOfMonths, portfolioDetermination);
    BigDecimal benchmarkCaptureReturn = calculateCaptureReturnFor(numberOfMonths, benchmarkDetermination);
    if (ZERO.compareTo(benchmarkCaptureReturn) == 0) {
      return ZERO;
    }
    return divide(portfolioCaptureReturn, benchmarkCaptureReturn).multiply(HUNDRED);
  }

  public BigDecimal calculateCaptureReturnFor(int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> determinations) {
    return calculateCumulativeCompoundedReturn(getBenchmarkValues(numberOfMonths, determinations));
  }

  private static BigDecimal calculateCumulativeCompoundedReturn(List<BigDecimal> returnFactors) {
    return returnFactors.stream().reduce(ONE, BigDecimal::multiply).subtract(ONE);
  }

}
