package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.RiskFreeWindowValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.pow;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static java.math.BigDecimal.ONE;

public class TrailingTotalReturnsCalculation extends PeriodCalculationAbstract<TrailingTotalReturnsResult, BigDecimal> {

  private final NavigableMap<LocalDate, BigDecimal> tBills;

  private TrailingTotalReturnsCalculation(PeriodCalculationInput input,
      Set<String> defaultPeriods,
      NavigableMap<LocalDate, BigDecimal> tBills) {
    super(input, defaultPeriods);
    this.tBills = tBills;
  }

  /**
   * Product-math-only variant for internal composition (Information Ratio, Rolling Total Returns, Distribution, MAR)
   * that reuse the TTR geometric product but do not carry the spec's T-Bill precondition.
   */
  public static TrailingTotalReturnsCalculation mathOnly(PeriodCalculationInput input, Set<String> defaultPeriods) {
    return new TrailingTotalReturnsCalculation(input, defaultPeriods, null);
  }

  /**
   * Standalone Trailing Total Returns variant that enforces per-date T-Bill coverage over the requested window.
   */
  public static TrailingTotalReturnsCalculation withTBillPrecondition(PeriodCalculationInput input,
      Set<String> defaultPeriods,
      NavigableMap<LocalDate, BigDecimal> tBills) {
    return new TrailingTotalReturnsCalculation(input, defaultPeriods, tBills);
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(int numberOfMonths) {
    return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
  }

  public BigDecimal calculatePeriodForNumberOfMonths(int numberOfMonths,
      NavigableMap<LocalDate, BigDecimal> totalReturns) {
    if (numberOfMonths > totalReturns.size()) {
      return null;
    }
    if (tBills != null) {
      LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, totalReturns);
      RiskFreeWindowValidator.requireCoverage(getSubMapByPeriodStartDate(periodStartDate, totalReturns), tBills);
    }
    BigDecimal product = calculateProductForPeriod(numberOfMonths, totalReturns);
    if (numberOfMonths < 12) {
      return product.subtract(ONE);
    }
    BigDecimal annualizedP = divide(TWELVE, BigDecimal.valueOf(numberOfMonths));
    return pow(product, annualizedP).subtract(ONE);
  }

  @Override
  public TrailingTotalReturnsResult defineResponseType(Set<Pair<String, BigDecimal>> periodValues) {
    return new TrailingTotalReturnsResult(formTimeIntervalResult(periodValues));
  }

}