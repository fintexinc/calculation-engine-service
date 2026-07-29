package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.returns.LeadingTotalReturnsResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.pow;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;

public class LeadingTotalReturnsCalculation extends PeriodCalculationAbstract<LeadingTotalReturnsResult, BigDecimal> {

  public LeadingTotalReturnsCalculation(final PeriodCalculationInput context,
      final Set<TimePeriod> defaultPeriods) {
    super(context, defaultPeriods);
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
