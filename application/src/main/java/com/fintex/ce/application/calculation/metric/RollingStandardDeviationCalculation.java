package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RollingAbstractCalculation;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.model.result.PeriodResult;
import com.fintex.ce.domain.model.result.RollingStandardDeviationResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

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
