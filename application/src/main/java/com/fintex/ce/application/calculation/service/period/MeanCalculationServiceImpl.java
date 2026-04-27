package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MeanCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.MeanResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.application.util.DecimalUtils.OUTPUT_SCALE;

@Service
public class MeanCalculationServiceImpl extends PeriodAbstractService<MeanResult, PeriodCommand> {

  public MeanCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MEAN;
  }

  public MeanCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    return new MeanCalculation(inputDTO, defaultPeriods).setScale(OUTPUT_SCALE);
  }

}
