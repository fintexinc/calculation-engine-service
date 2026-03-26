package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.model.result.SharpeRatioResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.divide;
import static java.math.BigDecimal.ZERO;

@Slf4j
public class SharpeRatioCalculation extends PeriodCalculationAbstract<SharpeRatioResult, BigDecimal> {

  public NavigableMap<LocalDate, BigDecimal> tBills;

  public StandardDeviationCalculation<SharpeRatioResult> standardDeviationCalculation;

  public SharpeRatioCalculation(final CalculationDTO input,
      final Set<String> defaultPeriods,
      final NavigableMap<LocalDate, BigDecimal> tBills,
      final StandardDeviationCalculation<SharpeRatioResult> standardDeviationCalculation) {
    super(input, defaultPeriods);
    this.tBills = tBills;
    this.standardDeviationCalculation = standardDeviationCalculation;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
  }

  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    if (numberOfMonths > returns.size()
        || numberOfMonths > this.tBills.size()
        || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, returns);
    final BigDecimal annualizedPortfolioReturn = calculateAverageArithmeticAnnualizedReturn(returns, periodStartDate,
        numberOfMonths);
    final BigDecimal annualizedRiskFreeRate = calculateAverageArithmeticAnnualizedReturn(
        restrictTBillsRange(tBills, returns), periodStartDate, numberOfMonths);
    final BigDecimal standardDeviation = getStandardDeviation(numberOfMonths, returns);
    return calculateSharpeRatio(annualizedPortfolioReturn, annualizedRiskFreeRate, standardDeviation);
  }

  /**
   * Calculates standard deviation by period
   *
   * @param numberOfMonths
   *          number of month in period
   * @return standard deviation by period
   */
  public BigDecimal getStandardDeviation(final int numberOfMonths, final SortedMap<LocalDate, BigDecimal> returns) {
    final NavigableMap<LocalDate, BigDecimal> excessReturn = calculateExcessReturn(returns, tBills);
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
  public BigDecimal calculateSharpeRatio(final BigDecimal annualizedPortfolioReturn,
      final BigDecimal annualizedRiskFreeRate,
      final BigDecimal standardDeviation) {
    log.debug("annualizedPortfolioReturn: {}, annualizedRiskFreeRate: {}, standardDeviation: {}",
        annualizedPortfolioReturn, annualizedRiskFreeRate, standardDeviation);
    if (standardDeviation.compareTo(ZERO) == 0) {
      return null;
    }
    return divide(annualizedPortfolioReturn.subtract(annualizedRiskFreeRate), standardDeviation);
  }

  @Override
  public SharpeRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final SharpeRatioResult sharpeRatioResDTO = new SharpeRatioResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    sharpeRatioResDTO.setSharpeRatio(timeIntervals);
    return sharpeRatioResDTO;
  }

}
