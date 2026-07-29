package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RollingAbstractCalculation;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class RollingTotalReturnsCalculation extends RollingAbstractCalculation<RollingTotalReturnsResult> {

  private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;

  public RollingTotalReturnsCalculation(final PeriodCalculationInput input,
      final Set<TimePeriod> defaultPeriods,
      final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation) {
    super(input, defaultPeriods);
    this.trailingTotalReturnsCalculation = trailingTotalReturnsCalculation;
  }

  @Override
  public BigDecimal calculateRollingValue(final int numberOfMonths, final NavigableMap<LocalDate, BigDecimal> returns) {
    return trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, returns);
  }

  @Override
  public RollingTotalReturnsResult defineResponseType(
      final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> periodValues) {
    final var result = new RollingTotalReturnsResult();
    final var rollingIntervalResultS = getRollingIntervalResults(periodValues);
    result.setRollingTotalReturns(rollingIntervalResultS);
    return result;
  }

}
