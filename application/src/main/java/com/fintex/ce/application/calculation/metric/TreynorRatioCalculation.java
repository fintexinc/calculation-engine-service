package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.TreynorRatioResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;

@Slf4j
public class TreynorRatioCalculation extends PeriodCalculationAbstract<TreynorRatioResult, BigDecimal> {

  private final NavigableMap<LocalDate, BigDecimal> tBills;
  private final BetaCalculation betaCalculation;

  public TreynorRatioCalculation(final PeriodCalculationInput input,
      final Set<String> defaultPeriods,
      final NavigableMap<LocalDate, BigDecimal> tBills,
      final BetaCalculation betaCalculation) {
    super(input, defaultPeriods);
    // Pre-restrict T-Bills to the portfolio date range once at construction (matches Sortino). Without this,
    // subsequent getSubMapByPeriodStartDate / lastKey calls on a non-overlapping T-Bill series throw
    // NoSuchElementException (or IllegalArgumentException for partial overlap), surfacing as HTTP 500 instead of
    // the documented null/RET-008 contract.
    this.tBills = restrictTBillsRange(tBills);
    this.betaCalculation = betaCalculation;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(int numberOfMonths) {
    if (numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths > tBills.size()
        || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
    validateTBillsCoverage(getSubMapByPeriodStartDate(periodStartDate, getPortfolioTotalReturns()), tBills);
    final BigDecimal annualizedPortfolioReturn = calculateAverageArithmeticAnnualizedReturn(getPortfolioTotalReturns(),
        periodStartDate, numberOfMonths);
    final BigDecimal annualizedRiskFreeRate = calculateAverageArithmeticAnnualizedReturn(tBills,
        periodStartDate, numberOfMonths);
    final BigDecimal beta = betaCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
    if (Objects.isNull(beta)) {
      return null;
    }
    return calculateTreynorRatio(annualizedPortfolioReturn, annualizedRiskFreeRate, beta);
  }

  @Override
  public TreynorRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    final var result = new TreynorRatioResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(periodValues);
    result.setTreynorRatio(timeIntervals);
    return result;
  }

  public BigDecimal calculateTreynorRatio(final BigDecimal annualizedPortfolioReturn,
      final BigDecimal annualizedRiskFreeRate,
      final BigDecimal beta) {
    log.info("annualizedPortfolioReturn: {}, annualizedRiskFreeRate: {}, beta: {}",
        annualizedPortfolioReturn, annualizedRiskFreeRate, beta);
    if (Objects.isNull(beta) || BigDecimal.ZERO.compareTo(beta) == 0) {
      return null;
    }
    return divide(annualizedPortfolioReturn.subtract(annualizedRiskFreeRate), beta);
  }

  /**
   * Treynor returns null whenever its composed {@link BetaCalculation} returns null (benchmark or excess-return series
   * shorter than the period) or when the T-Bill series is shorter than the period. Without this override
   * {@code availableMonths()} would fall back to {@code portfolioTotalReturns.size()} and {@code RET-008} would be
   * silently skipped for those cases. Treynor sits outside the {@code BenchmarkWeightedAverageCalculation} chain, so
   * the inherited overrides don't reach it — delegate to the composed Beta and fold in {@code tBills} explicitly.
   */
  @Override
  public int availableMonths() {
    return Math.min(super.availableMonths(),
        Math.min(betaCalculation.availableMonths(), tBills.size()));
  }

}
