package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.BenchmarkWeightedAverageCalculation;
import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.returns.ExcessReturnsResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;

public class ExcessReturnsCalculation extends BenchmarkWeightedAverageCalculation<ExcessReturnsResult, BigDecimal> {

  public ExcessReturnsCalculation(final BenchmarkPeriodCalculationInput input,
      final Set<String> periods) {
    super(input, periods);
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getBenchmarkTotalReturns().size()
        || numberOfMonths > getPortfolioTotalReturns().size() || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final BigDecimal portfolioAnnualizedReturn = calculateAnnualizedReturnsByPeriod(numberOfMonths,
        getPortfolioTotalReturns());
    final BigDecimal benchmarkAnnualizedReturn = calculateAnnualizedReturnsByPeriod(numberOfMonths,
        getBenchmarkTotalReturns());
    return Objects.nonNull(portfolioAnnualizedReturn) && Objects.nonNull(benchmarkAnnualizedReturn)
        ? DecimalUtils.toUserScale(portfolioAnnualizedReturn.subtract(benchmarkAnnualizedReturn))
        : null;
  }

  @Override
  public ExcessReturnsResult defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    return new ExcessReturnsResult(formTimeIntervalResult(periodValues));
  }

  /**
   * calculates annualized returns for portfolio or benchmark return factor
   *
   * @param numberOfMonths
   *          number of month in period
   * @param returnFactor
   *          portfolio or benchmark returns factor
   * @return calculates annualized returns
   */
  public BigDecimal calculateAnnualizedReturnsByPeriod(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returnFactor) {
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, returnFactor);
    final SortedMap<LocalDate, BigDecimal> subMapByPeriodStartDate = getSubMapByPeriodStartDate(periodStartDate,
        returnFactor);
    final BigDecimal product = CalculationUtils.product(subMapByPeriodStartDate);
    final BigDecimal pow = DecimalUtils.pow(DecimalUtils.toUserScale(product), getPower(numberOfMonths));
    return pow.subtract(BigDecimal.ONE);
  }

  /**
   * returns power value. ^12/numberOfMonths
   *
   * @param numberOfMonths
   *          number of month in period
   * @return calculated power value
   */
  public BigDecimal getPower(final int numberOfMonths) {
    return DecimalUtils.divide(TWELVE, BigDecimal.valueOf(numberOfMonths));
  }

}
