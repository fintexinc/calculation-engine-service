package ca.tangerine.pce.application.calculation.metric;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

import static ca.tangerine.pce.application.util.DecimalUtils.divide;
import static ca.tangerine.pce.application.util.DecimalUtils.pow;
import static ca.tangerine.pce.model.util.BigDecimalConstants.TWELVE;
import static java.math.BigDecimal.ONE;

import ca.tangerine.pce.application.calculation.metric.core.PeriodCalculationAbstract;
import ca.tangerine.pce.application.util.RiskFreeWindowValidator;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.result.returns.TrailingTotalReturnsResult;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

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
  public TrailingTotalReturnsResult defineResponseType(Set<Pair<String, BigDecimal>> periodValues) {
    return new TrailingTotalReturnsResult(formTimeIntervalResult(periodValues));
  }

}
