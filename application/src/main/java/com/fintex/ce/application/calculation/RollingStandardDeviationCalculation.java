package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.core.RollingAbstractCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.result.RollingStandardDeviationResult;
import com.fintex.ce.port.input.result.PeriodResult;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class RollingStandardDeviationCalculation extends RollingAbstractCalculation<RollingStandardDeviationResult> {

  private final StandardDeviationCalculation<PeriodResult> standardDeviationCalculation;

  public RollingStandardDeviationCalculation(final CalculationDTO input,
      final Set<String> defaultPeriods,
      final StandardDeviationCalculation<PeriodResult> standardDeviationCalculation) {
    super(input, defaultPeriods);
    this.standardDeviationCalculation = standardDeviationCalculation;
  }

  @Override
  public BigDecimal calculateRollingValue(final int numberOfMonths, final NavigableMap<LocalDate, BigDecimal> returns) {
    return standardDeviationCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, returns);
  }

  @Override
  public RollingStandardDeviationResult defineResponseType(
      final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> result) {
    final var rollingStandardDeviationResult = new RollingStandardDeviationResult();
    final var rollingIntervalResultS = getRollingIntervalResults(result);
    rollingStandardDeviationResult.setRollingStandardDeviation(rollingIntervalResultS);
    return rollingStandardDeviationResult;
  }

}
