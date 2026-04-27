package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;

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
