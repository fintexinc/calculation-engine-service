package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.core.AlphaBetaCalculationAbstract;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.result.AlphaResult;
import com.fintex.ce.port.input.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.CalculationUtils.average;

public class AlphaCalculation extends AlphaBetaCalculationAbstract<AlphaResult> {

  public AlphaCalculation(final BenchmarkCalculationDTO input,
      final Set<String> periods,
      final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
      final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
    super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
  }

  @Override
  public AlphaResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final AlphaResult resDTO = new AlphaResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    resDTO.setAlpha(timeIntervals);
    return resDTO;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getBenchmarkTotalReturns().size()
        || numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
    final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod = getSubMapByPeriodStartDate(periodStartDate,
        getPortfolioExcessReturn());
    final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod = getSubMapByPeriodStartDate(periodStartDate,
        getBenchmarkExcessReturn());
    final BigDecimal portfolioExcessAverage = average(portfolioExcessReturnByPeriod);
    final BigDecimal benchmarkExcessAverage = average(benchmarkExcessReturnByPeriod);
    return calculateAlpha(numberOfMonths, portfolioExcessAverage, benchmarkExcessAverage);
  }

  /**
   * Calculates the final result for Alpha calculation. The formula is (portfolioExcessAverage - (beta *
   * benchmarkExcessAverage)) * 12
   *
   * @param numberOfMonth
   *          number of month for some period.
   * @param portfolioExcessAverage
   *          Average Excess Portfolio Return.
   * @param benchmarkExcessAverage
   *          Average Excess Benchmark Return.
   * @return final Alpha result.
   */
  public BigDecimal calculateAlpha(final int numberOfMonth,
      final BigDecimal portfolioExcessAverage,
      final BigDecimal benchmarkExcessAverage) {
    final BigDecimal beta = calculateBeta(numberOfMonth);
    final BigDecimal alpha = portfolioExcessAverage.subtract(beta.multiply(benchmarkExcessAverage));
    return alpha.multiply(TWELVE);
  }

  /**
   * Returns calculated beta result.
   *
   * @param numberOfMonth
   *          number of month for some period.
   * @return final beta result.
   */
  public BigDecimal calculateBeta(final int numberOfMonth) {
    return super.calculatePeriodForNumberOfMonths(numberOfMonth);
  }

}
