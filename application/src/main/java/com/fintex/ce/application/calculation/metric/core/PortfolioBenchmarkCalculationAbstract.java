package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.util.BigDecimalConstants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import lombok.Getter;

import static com.fintex.ce.application.util.CalculationUtils.average;

@Getter
abstract class PortfolioBenchmarkCalculationAbstract<T extends PeriodResult>
    extends
      BenchmarkWeightedAverageCalculation<T, BigDecimal> {
  public NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn;
  public NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn;

  protected PortfolioBenchmarkCalculationAbstract(final BenchmarkPeriodCalculationInput input,
      final Set<String> periods,
      final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
      final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
    super(input, periods);
    this.portfolioExcessReturn = portfolioExcessReturn;
    this.benchmarkExcessReturn = benchmarkExcessReturn;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (isNumberOfMonthsInvalid(numberOfMonths)) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
    final SortedMap<LocalDate, BigDecimal> portfolioWindow = getSubMapByPeriodStartDate(periodStartDate,
        getPortfolioTotalReturns());
    validateTBillsCoverage(portfolioWindow, portfolioExcessReturn);
    validateTBillsCoverage(portfolioWindow, benchmarkExcessReturn);
    final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod = getSubMapByPeriodStartDate(periodStartDate,
        portfolioExcessReturn);
    final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod = getSubMapByPeriodStartDate(periodStartDate,
        benchmarkExcessReturn);
    final BigDecimal portfolioExcessAverage = average(portfolioExcessReturnByPeriod);
    return calculatePeriod(portfolioExcessReturnByPeriod, benchmarkExcessReturnByPeriod, portfolioExcessAverage);
  }

  protected abstract BigDecimal calculatePeriod(
      final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
      final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod,
      final BigDecimal portfolioExcessAverage);

  private boolean isNumberOfMonthsInvalid(final int numberOfMonths) {
    return numberOfMonths > getBenchmarkTotalReturns().size()
        || numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths > portfolioExcessReturn.size()
        || numberOfMonths > benchmarkExcessReturn.size()
        || numberOfMonths < BigDecimalConstants.TWELVE.intValue();
  }

  @Override
  public int availableMonths() {
    return Math.min(super.availableMonths(),
        Math.min(portfolioExcessReturn.size(), benchmarkExcessReturn.size()));
  }
}
