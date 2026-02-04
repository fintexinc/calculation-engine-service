package com.fintex.ce.application.calculation.core;

import com.fintex.ce.domain.constant.BigDecimalConstants;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.result.PeriodResult;
import com.fintex.ce.util.DecimalUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;

import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ZERO;

/**
 * Abstraction for RSquared calculations
 *
 * @param <T>
 *          response type.
 */
public abstract class RSquaredCalculationAbstract<T extends PeriodResult>
    extends
      PortfolioBenchmarkCalculationAbstract<T> {

  protected RSquaredCalculationAbstract(final BenchmarkCalculationDTO input,
      final Set<String> periods,
      final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
      final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
    super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
  }

  @Override
  public BigDecimal calculatePeriod(
      final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
      final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod,
      final BigDecimal portfolioExcessAverage) {
    return calculateRSquared(portfolioExcessReturnByPeriod, benchmarkExcessReturnByPeriod, portfolioExcessAverage);
  }

  /**
   * calculates RSquared,
   *
   * @param portfolioExcessReturnByPeriod
   *          portfolio excess return values from start of period to the end
   * @param benchmarkExcessReturnByPeriod
   *          benchmark excess return values from start of period to the end
   * @param portfolioExcessAverage
   *          average value of portfolioExcessReturnByPeriod
   * @return rsquared value
   */
  public BigDecimal calculateRSquared(final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
      final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod,
      final BigDecimal portfolioExcessAverage) {
    final BigDecimal sumSquaredRegression = calculateSumSquaredRegression(portfolioExcessReturnByPeriod,
        benchmarkExcessReturnByPeriod);
    final BigDecimal totalSumOfSquares = calculateTotalSumOfSquares(portfolioExcessReturnByPeriod,
        portfolioExcessAverage);
    return toUserScale(BigDecimalConstants.ONE.subtract(DecimalUtils.divide(sumSquaredRegression, totalSumOfSquares)));
  }

  /**
   * calculates numerator for RSquared formula.
   *
   * @param portfolioExcessReturnByPeriod
   *          portfolio excess return values from start of period to the end
   * @param benchmarkExcessReturnByPeriod
   *          benchmark excess return values from start of period to the end
   * @return sumSquaredRegression value
   */
  public BigDecimal calculateSumSquaredRegression(final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
      final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod) {
    return portfolioExcessReturnByPeriod.entrySet().stream().map(e -> (e.getValue().subtract(
        benchmarkExcessReturnByPeriod.get(e.getKey()))).pow(2))
        .reduce(ZERO, BigDecimal::add);
  }

  /**
   * calculates totalSumOfSquares for RSquared formula.
   *
   * @param portfolioExcessReturnByPeriod
   *          portfolio excess return values from start of period to the end
   * @param portfolioExcessAverage
   *          average value of portfolioExcessReturnByPeriod
   * @return totalSumOfSquares value
   */
  public BigDecimal calculateTotalSumOfSquares(final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
      final BigDecimal portfolioExcessAverage) {
    return portfolioExcessReturnByPeriod.values().stream().map(bigDecimal -> (bigDecimal.subtract(
        portfolioExcessAverage)).pow(2))
        .reduce(ZERO, BigDecimal::add);
  }
}
