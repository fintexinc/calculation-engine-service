package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.result.LeadingTotalReturnsResult;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.domain.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.pow;
import static java.math.BigDecimal.ONE;

public class LeadingTotalReturnsCalculation extends PeriodCalculationAbstract<LeadingTotalReturnsResult, BigDecimal> {

  public LeadingTotalReturnsCalculation(final CalculationDTO calculationDTO,
      final Set<String> defaultPeriods) {
    super(calculationDTO, defaultPeriods);
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getPortfolioTotalReturns().size()) {
      return null;
    }
    final BigDecimal product = calculateProductForPeriod(numberOfMonths, getPortfolioTotalReturns());
    if (numberOfMonths < 12) {
      return product.subtract(ONE);
    }
    final BigDecimal annualizedP = divide(TWELVE, BigDecimal.valueOf(numberOfMonths));
    return pow(product, annualizedP).subtract(ONE);
  }

  @Override
  public LeadingTotalReturnsResult defineResponseType(final Set<Pair<String, BigDecimal>> periodAndLeadingReturn) {
    final var result = new LeadingTotalReturnsResult();
    final var timeIntervals = formTimeIntervalResult(periodAndLeadingReturn);
    result.setLeadingTotalReturn(timeIntervals);
    return result;
  }

  @Override
  public NavigableMap<LocalDate, BigDecimal> filterRequiredMonthsForPeriod(final long numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    final LocalDate endDate = toLastDayOfMonth(returns.firstKey().plusMonths(numberOfMonths - 1));
    return returns.headMap(endDate, true);
  }
}
