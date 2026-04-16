package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.SortinoRatioResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.divide;
import static java.math.BigDecimal.ZERO;

public class SortinoRatioCalculation extends PeriodCalculationAbstract<SortinoRatioResult, BigDecimal> {

  public DownsideDeviationCalculation<SortinoRatioResult> downsideDeviationCalculation;
  public NavigableMap<LocalDate, BigDecimal> tBills;

  public SortinoRatioCalculation(final CalculationDTO input,
      final Set<String> defaultPeriods,
      final NavigableMap<LocalDate, BigDecimal> tBills,
      final DownsideDeviationCalculation<SortinoRatioResult> downsideDeviationCalculation) {
    super(input, defaultPeriods);
    this.tBills = restrictTBillsRange(tBills);
    this.downsideDeviationCalculation = downsideDeviationCalculation;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths > this.tBills.size()
        || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
    final BigDecimal annualizedPortfolioReturn = calculateAverageArithmeticAnnualizedReturn(getPortfolioTotalReturns(),
        periodStartDate, numberOfMonths);
    final BigDecimal annualizedRiskFreeRate = calculateAverageArithmeticAnnualizedReturn(tBills, periodStartDate,
        numberOfMonths);
    final BigDecimal downsideDeviation = getDownsideDeviation(numberOfMonths);
    return calculateSortinoRatio(annualizedPortfolioReturn, annualizedRiskFreeRate, downsideDeviation);
  }

  @Override
  public SortinoRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final SortinoRatioResult sortinoRatioResDTO = new SortinoRatioResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    sortinoRatioResDTO.setSortinoRatio(timeIntervals);
    return sortinoRatioResDTO;
  }

  /**
   * Method is used for getting the final result of Downside Deviation calculation by specified period.
   *
   * @param numberOfMonths
   *          number of month in the period.
   * @return final result of Downside Deviation by specified period.
   */
  public BigDecimal getDownsideDeviation(final int numberOfMonths) {
    return downsideDeviationCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
  }

  /**
   * Method is used to calculate the final result of Sortino Ratio calculation.
   * <p>
   * The formula is: (annualizedPortfolioReturn - annualizedRiskFreeRate) / Downside Deviation
   *
   * @param annualizedPortfolioReturn
   *          annualized portfolio return value.
   * @param annualizedRiskFreeRate
   *          annualized risk free rate value.
   * @param downsideDeviation
   *          calculated Downside Deviation value.
   * @return calculated Sortino Ratio result.
   */
  public BigDecimal calculateSortinoRatio(final BigDecimal annualizedPortfolioReturn,
      final BigDecimal annualizedRiskFreeRate,
      final BigDecimal downsideDeviation) {
    final BigDecimal diff = annualizedPortfolioReturn.subtract(annualizedRiskFreeRate);
    if (downsideDeviation.compareTo(ZERO) == 0) {
      return null;
    }
    return divide(diff, downsideDeviation);
  }

}
