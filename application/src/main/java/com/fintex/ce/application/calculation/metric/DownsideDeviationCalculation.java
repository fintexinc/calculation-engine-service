package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.RiskFreeWindowValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.risk.DownsideDeviationResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.CalculationUtils.sum;
import static com.fintex.ce.application.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.pow;
import static com.fintex.ce.application.util.DecimalUtils.squareRoot;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;

public class DownsideDeviationCalculation<T extends PeriodResult> extends PeriodCalculationAbstract<T, BigDecimal> {

  public NavigableMap<LocalDate, BigDecimal> tBills;
  public NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn;

  public DownsideDeviationCalculation(final PeriodCalculationInput input,
      final Set<TimePeriod> defaultPeriods,
      final NavigableMap<LocalDate, BigDecimal> tBills) {
    super(input, defaultPeriods);
    this.tBills = restrictTBillsRange(tBills);
    portfolioExcessReturn = calculateExcessReturn(getPortfolioTotalReturns(), tBills);
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
    RiskFreeWindowValidator.requireCoverage(
        getSubMapByPeriodStartDate(periodStartDate, getPortfolioTotalReturns()), portfolioExcessReturn);
    final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnsInPeriod = getSubMapByPeriodStartDate(periodStartDate,
        portfolioExcessReturn);
    final TreeMap<LocalDate, BigDecimal> downsideReturnSquared = calculateDownsideReturnSquared(
        portfolioExcessReturnsInPeriod);
    return calculateDownsideDeviation(numberOfMonths, downsideReturnSquared);
  }

  @Override
  public T defineResponseType(final Map<String, BigDecimal> periodValues) {
    return (T) new DownsideDeviationResult(periodValues);
  }

  /**
   * calculates downside deviation by formula
   *
   * @param numberOfMonths
   *          number of month in period
   * @param downsideReturnSquared
   *          calculated downside return squared values
   * @return calculated value
   */
  public BigDecimal calculateDownsideDeviation(final int numberOfMonths,
      final TreeMap<LocalDate, BigDecimal> downsideReturnSquared) {
    return squareRoot(divide(sum(downsideReturnSquared), numberOfMonths)).multiply(squareRoot(TWELVE));
  }

  /**
   * calculates downside return squared values. (negativeValueInPeriod^2)
   *
   * @param portfolioExcessReturnsInPeriod
   *          portfolio excess returns values
   * @return squared values
   */
  public TreeMap<LocalDate, BigDecimal> calculateDownsideReturnSquared(
      final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnsInPeriod) {
    return portfolioExcessReturnsInPeriod.entrySet().stream()
        .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) < 0)
        .collect(toTreeMap(Map.Entry::getKey, e -> pow(e.getValue(), TWO)));
  }
}