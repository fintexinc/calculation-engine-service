package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.result.TrailingTotalReturnsResult;
import com.fintex.ce.application.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.domain.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.pow;
import static java.math.BigDecimal.ONE;

public class TrailingTotalReturnsCalculation extends PeriodCalculationAbstract<TrailingTotalReturnsResult, BigDecimal> {

  public TrailingTotalReturnsCalculation(final CalculationDTO input,
      final Set<String> defaultPeriods) {
    super(input, defaultPeriods);
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
  }

  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> totalReturns) {
    if (numberOfMonths > totalReturns.size()) {
      return null;
    }
    final BigDecimal product = calculateProductForPeriod(numberOfMonths, totalReturns);
    if (numberOfMonths < 12) {
      return product.subtract(ONE);
    }
    final BigDecimal annualizedP = divide(TWELVE, BigDecimal.valueOf(numberOfMonths));
    return pow(product, annualizedP).subtract(ONE);
  }

  @Override
  public TrailingTotalReturnsResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final TrailingTotalReturnsResult trailingTotalReturnsResDTO = new TrailingTotalReturnsResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    trailingTotalReturnsResDTO.setTrailingTotalReturn(timeIntervals);
    return trailingTotalReturnsResDTO;
  }

}
