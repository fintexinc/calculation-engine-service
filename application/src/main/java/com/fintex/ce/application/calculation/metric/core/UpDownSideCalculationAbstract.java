package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;

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
import static java.util.Optional.ofNullable;

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

  protected UpDownSideCalculationAbstract(BenchmarkCalculationDTO input, Set<String> periods) {
    super(input, periods);
    portfolioDetermination = getPortfolioDetermination();
    benchmarkDetermination = getBenchmarkDetermination(portfolioDetermination);
  }

  public abstract boolean filterCaptureExpression(final Map.Entry<LocalDate, BigDecimal> e);

  /**
   * Because size of getBenchmarkTotalReturns() and getPortfolioTotalReturns() could be different We handle that with
   * dummy values in returned result of this method. For example when getBenchmarkTotalReturns() consists of 180 entries
   * and getPortfolioTotalReturns() of 100 entries Then result of this method would contain 1..100 correct values and
   * 101-180 incorrect values. That is done by purpose to increase performance instead of calling this method each time
   * for each period. In this method calculatePeriodForNumberOfMonths(), exists validation that would not allow using
   * incorrect values (101-180)
   */
  public TreeMap<LocalDate, BigDecimal> getPortfolioDetermination() {
    return getBenchmarkTotalReturns().entrySet().stream().filter(this::filterCaptureExpression)
        .collect(toTreeMap(Map.Entry::getKey,
            e -> divide(ofNullable(getPortfolioTotalReturns().get(e.getKey())).orElse(HUNDRED), HUNDRED).add(ONE)));
  }

  /**
   * see comment of getPortfolioDetermination()
   */
  public NavigableMap<LocalDate, BigDecimal> getBenchmarkDetermination(
      final NavigableMap<LocalDate, BigDecimal> portfolioDownsideDetermination) {
    final NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = getBenchmarkTotalReturns();
    return portfolioDownsideDetermination.entrySet().stream()
        .collect(toTreeMap(Map.Entry::getKey,
            e -> divide(ofNullable(benchmarkTotalReturns.get(e.getKey())).orElse(HUNDRED), HUNDRED).add(ONE)));
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getBenchmarkTotalReturns().size()
        || numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths < 12) {
      return null;
    }
    final BigDecimal portfolioDeviation = calculateDeviationFor(numberOfMonths, portfolioDetermination);
    final BigDecimal benchmarkDeviation = calculateDeviationFor(numberOfMonths, benchmarkDetermination);
    if (ZERO.compareTo(benchmarkDeviation) == 0) {
      return ZERO;
    }
    return divide(portfolioDeviation, benchmarkDeviation).multiply(HUNDRED);
  }

  public BigDecimal calculateDeviationFor(int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> determinations) {
    final List<BigDecimal> portfolioProduct = getBenchmarkValues(numberOfMonths, determinations);
    if (portfolioProduct.isEmpty()) {
      return ZERO;
    }
    final BigDecimal division = divide(ONE, portfolioProduct.size());
    final BigDecimal product = portfolioProduct.stream().reduce(ONE, BigDecimal::multiply);
    return DecimalUtils.pow(product, division).subtract(ONE);
  }

}
