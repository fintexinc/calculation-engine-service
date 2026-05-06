package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RollingAbstractCalculation;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.RollingIntervalResult;
import com.fintex.ce.model.domain.result.rolling.RollingCorrelationResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static java.math.BigDecimal.ZERO;

public class RollingCorrelationCalculation extends RollingAbstractCalculation<RollingCorrelationResult> {

  private final CorrelationCalculation correlationCalculation;
  private final NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns;

  public RollingCorrelationCalculation(final PeriodCalculationInput input,
      final Set<String> defaultPeriods,
      final CorrelationCalculation correlationCalculation,
      final NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns) {
    super(input, defaultPeriods);
    this.correlationCalculation = correlationCalculation;
    this.benchmarkTotalReturns = benchmarkTotalReturns;
  }

  @Override
  public BigDecimal calculateRollingValue(final int numberOfMonths,
      NavigableMap<LocalDate, BigDecimal> portfolioReturns) {
    if (numberOfMonths > benchmarkTotalReturns.size()) {
      return null;
    }
    portfolioReturns = initializePortfolioReturns(portfolioReturns);
    final NavigableMap<LocalDate, BigDecimal> benchmarkReturns = initializeBenchmarkReturns(portfolioReturns);
    return correlationCalculation.calculateCorrelation(portfolioReturns, benchmarkReturns);
  }

  @Override
  public int availableMonths() {
    return Math.min(super.availableMonths(), benchmarkTotalReturns.size());
  }

  /**
   * If needed reinitializes Portfolio Returns.
   * <p>
   * Checks if Benchmarks Start Date greater than Portfolio Start Date. If yes - Benchmarks Start Date becomes common
   * Start Date for Portfolio, otherwise Common Performance Start Date is Portfolio Start Date.
   *
   * @param portfolioReturns
   *          rolling portfolio returns
   * @return rolling portfolio returns
   */
  public NavigableMap<LocalDate, BigDecimal> initializePortfolioReturns(
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns) {
    if (isBenchmarkStartDateGreaterThanPortfolioStartDate(portfolioReturns)) {
      return getReturns(portfolioReturns, portfolioTotalReturns);
    } else {
      return portfolioReturns;
    }
  }

  /**
   * Initializes Benchmark Returns.
   * <p>
   * Checks if Benchmarks Start Date greater than Portfolio Start Date. If yes - Benchmarks Start Date becomes common
   * Start Date for Benchmarks Returns, otherwise Common Performance Start Date is Portfolio Start Date.
   *
   * @param portfolioReturns
   *          rolling portfolio returns
   * @return initialized benchmark returns
   */
  public NavigableMap<LocalDate, BigDecimal> initializeBenchmarkReturns(
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns) {
    if (isBenchmarkStartDateGreaterThanPortfolioStartDate(portfolioReturns)) {
      return getReturns(portfolioReturns, benchmarkTotalReturns);
    } else {
      return benchmarkTotalReturns.subMap(portfolioReturns.firstKey(), true, portfolioReturns.lastKey(), true);
    }
  }

  /**
   * Takes a sub map from Total Portfolio or Benchmark Returns by Benchmark Start and End Dates.
   *
   * @param portfolioReturns
   *          rolling portfolio returns
   * @param totalReturns
   *          Portfolio or Benchmark Total Returns
   * @return portfolio or benchmark total returns submap
   */
  public NavigableMap<LocalDate, BigDecimal> getReturns(final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final NavigableMap<LocalDate, BigDecimal> totalReturns) {
    return totalReturns.subMap(benchmarkTotalReturns.firstKey(), true, benchmarkTotalReturns.firstKey().plusMonths(
        portfolioReturns.size()), false);
  }

  /**
   * Checks if Benchmarks Start Date is greater than Portfolio Start Date
   *
   * @param portfolioReturns
   *          Portfolio Returns.
   * @return true - if Benchmark Start Date is greater than Portfolio Start Date, otherwise - false
   */
  public boolean isBenchmarkStartDateGreaterThanPortfolioStartDate(
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns) {
    return benchmarkTotalReturns.firstKey().compareTo(portfolioReturns.firstKey()) > ZERO.intValue();
  }

  @Override
  public RollingCorrelationResult defineResponseType(
      final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> result) {
    final var rollingCorrelationResult = new RollingCorrelationResult();
    final Set<RollingIntervalResult> rollingIntervalResultS = getRollingIntervalResults(result);
    rollingCorrelationResult.setRollingCorrelation(rollingIntervalResultS);
    return rollingCorrelationResult;
  }
}
