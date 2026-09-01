package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.RiskFreeWindowValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.pow;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static java.math.BigDecimal.ONE;

public class TrailingTotalReturnsCalculation extends PeriodCalculationAbstract<TrailingTotalReturnsResult, BigDecimal> {

  private final boolean requireTBillsCoverage;
  private final NavigableMap<LocalDate, BigDecimal> tBills;

  public static TrailingTotalReturnsCalculation mathOnly(PeriodCalculationInput input, Set<TimePeriod> defaultPeriods) {
    return new TrailingTotalReturnsCalculation(input, defaultPeriods, null, false);
  }

  public static TrailingTotalReturnsCalculation withTBillPrecondition(PeriodCalculationInput input,
      Set<TimePeriod> defaultPeriods,
      NavigableMap<LocalDate, BigDecimal> tBills) {
    return new TrailingTotalReturnsCalculation(input, defaultPeriods, tBills, true);
  }

  private TrailingTotalReturnsCalculation(PeriodCalculationInput input,
      Set<TimePeriod> defaultPeriods,
      NavigableMap<LocalDate, BigDecimal> tBills,
      boolean requireTBillsCoverage) {
    super(input, defaultPeriods);
    this.requireTBillsCoverage = requireTBillsCoverage;
    this.tBills = requireTBillsCoverage ? Objects.requireNonNull(tBills) : null;
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
    if (requireTBillsCoverage) {
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
  public TrailingTotalReturnsResult defineResponseType(Map<String, BigDecimal> periodValues) {
    return new TrailingTotalReturnsResult(periodValues);
  }

}
