package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RollingAbstractCalculation;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class RollingTotalReturnsCalculation extends RollingAbstractCalculation<RollingTotalReturnsResult> {

  private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;

  public RollingTotalReturnsCalculation(final CalculationDTO input,
      final Set<String> defaultPeriods,
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
      final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> result) {
    final var rollingTotalReturnsResDTO = new RollingTotalReturnsResult();
    final var rollingIntervalResultS = getRollingIntervalResults(result);
    rollingTotalReturnsResDTO.setRollingTotalReturns(rollingIntervalResultS);
    return rollingTotalReturnsResDTO;
  }

}
