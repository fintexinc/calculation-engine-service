package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.StandardDeviationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;

@Service
public class StandardDeviationCalculationServiceImpl
    extends
      PeriodAbstractService<StandardDeviationResult, PeriodCommand> {

  public StandardDeviationCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.STANDARD_DEVIATION;
  }

  public StandardDeviationCalculation defineCalculationMethod(final PeriodCommand command) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    return new StandardDeviationCalculation(context, defaultPeriods).setScale(OUTPUT_SCALE);
  }

}
