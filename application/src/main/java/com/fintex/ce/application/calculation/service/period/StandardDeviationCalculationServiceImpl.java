package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.StandardDeviationResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.application.util.DecimalUtils.OUTPUT_SCALE;

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

  public StandardDeviationCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    return new StandardDeviationCalculation(inputDTO, defaultPeriods).setScale(OUTPUT_SCALE);
  }

}
