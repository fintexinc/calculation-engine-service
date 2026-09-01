package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RollingAbstractCalculation;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.rolling.RollingStandardDeviationResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

public class RollingStandardDeviationCalculation extends RollingAbstractCalculation<RollingStandardDeviationResult> {

  private final StandardDeviationCalculation<PeriodResult> standardDeviationCalculation;

  public RollingStandardDeviationCalculation(final PeriodCalculationInput input,
      final Set<TimePeriod> defaultPeriods,
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
      final Map<String, NavigableMap<LocalDate, BigDecimal>> result) {
    final var rollingStandardDeviationResult = new RollingStandardDeviationResult();
    final var rollingIntervalResultS = getRollingIntervalResults(result);
    rollingStandardDeviationResult.setRollingStandardDeviation(rollingIntervalResultS);
    return rollingStandardDeviationResult;
  }

}
