package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.RiskFreeWindowValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.TreynorRatioResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
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
      final Set<TimePeriod> defaultPeriods,
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
        || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
    RiskFreeWindowValidator.requireCoverage(
        getSubMapByPeriodStartDate(periodStartDate, getPortfolioTotalReturns()), tBills);
    BigDecimal annualizedPortfolioReturn = calculateAverageArithmeticAnnualizedReturn(getPortfolioTotalReturns(),
        periodStartDate, numberOfMonths);
    BigDecimal annualizedRiskFreeRate = calculateAverageArithmeticAnnualizedReturn(tBills,
        periodStartDate, numberOfMonths);
    BigDecimal beta = betaCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
    if (Objects.isNull(beta)) {
      return null;
    }
    return calculateTreynorRatio(annualizedPortfolioReturn, annualizedRiskFreeRate, beta);
  }

  @Override
  public TreynorRatioResult defineResponseType(final Map<String, BigDecimal> periodValues) {
    var result = new TreynorRatioResult();
    result.setTreynorRatio(periodValues);
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
   * shorter than the period). Without this override {@code availableMonths()} would fall back to
   * {@code portfolioTotalReturns.size()} and {@code RET-008} would be silently skipped for that case. Treynor sits
   * outside the {@code BenchmarkWeightedAverageCalculation} chain, so the inherited overrides don't reach it â€”
   * delegate to the composed Beta explicitly. T-Bill coverage is enforced separately by
   * {@link RiskFreeWindowValidator#requireCoverage} inside {@link #calculatePeriodForNumberOfMonths}.
   */
  @Override
  public int availableMonths() {
    return Math.min(super.availableMonths(), betaCalculation.availableMonths());
  }

}
