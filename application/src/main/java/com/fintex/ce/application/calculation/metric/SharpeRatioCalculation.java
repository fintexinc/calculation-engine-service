package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.RiskFreeWindowValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import lombok.extern.slf4j.Slf4j;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static java.math.BigDecimal.ZERO;

@Slf4j
public class SharpeRatioCalculation extends PeriodCalculationAbstract<SharpeRatioResult, BigDecimal> {

  public NavigableMap<LocalDate, BigDecimal> tBills;

  public StandardDeviationCalculation<SharpeRatioResult> standardDeviationCalculation;

  public SharpeRatioCalculation(PeriodCalculationInput input,
      Set<String> defaultPeriods,
      NavigableMap<LocalDate, BigDecimal> tBills,
      StandardDeviationCalculation<SharpeRatioResult> standardDeviationCalculation) {
    super(input, defaultPeriods);
    this.tBills = restrictTBillsRange(tBills);
    this.standardDeviationCalculation = standardDeviationCalculation;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(int numberOfMonths) {
    return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
  }

  public BigDecimal calculatePeriodForNumberOfMonths(int numberOfMonths,
      NavigableMap<LocalDate, BigDecimal> returns) {
    if (numberOfMonths > returns.size() || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, returns);
    RiskFreeWindowValidator.requireCoverage(getSubMapByPeriodStartDate(periodStartDate, returns), tBills);
    BigDecimal annualizedPortfolioReturn = calculateAverageArithmeticAnnualizedReturn(returns, periodStartDate,
        numberOfMonths);
    BigDecimal annualizedRiskFreeRate = calculateAverageArithmeticAnnualizedReturn(
        restrictTBillsRange(tBills, returns), periodStartDate, numberOfMonths);
    BigDecimal standardDeviation = getStandardDeviation(numberOfMonths, returns);
    return calculateSharpeRatio(annualizedPortfolioReturn, annualizedRiskFreeRate, standardDeviation);
  }

  /**
   * Calculates standard deviation by period
   *
   * @param numberOfMonths
   *          number of month in period
   * @return standard deviation by period
   */
  public BigDecimal getStandardDeviation(int numberOfMonths, SortedMap<LocalDate, BigDecimal> returns) {
    NavigableMap<LocalDate, BigDecimal> excessReturn = calculateExcessReturn(returns, tBills);
    return standardDeviationCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, excessReturn);
  }

  /**
   * calculates sharpe ratio (annualizedPortfolioReturn - annualizedRiskFreeRate) / standardDeviation
   *
   * @param annualizedPortfolioReturn
   *          annualized portfolio return value
   * @param annualizedRiskFreeRate
   *          annualized risk free rate value
   * @param standardDeviation
   *          calculated standard deviation value
   * @return calculated sharpe ratio
   */
  public BigDecimal calculateSharpeRatio(BigDecimal annualizedPortfolioReturn,
      BigDecimal annualizedRiskFreeRate,
      BigDecimal standardDeviation) {
    log.debug("annualizedPortfolioReturn: {}, annualizedRiskFreeRate: {}, standardDeviation: {}",
        annualizedPortfolioReturn, annualizedRiskFreeRate, standardDeviation);
    if (standardDeviation.compareTo(ZERO) == 0) {
      return null;
    }
    return divide(annualizedPortfolioReturn.subtract(annualizedRiskFreeRate), standardDeviation);
  }

  @Override
  public SharpeRatioResult defineResponseType(Set<Pair<String, BigDecimal>> periodValues) {
    return new SharpeRatioResult(formTimeIntervalResult(periodValues));
  }

}
