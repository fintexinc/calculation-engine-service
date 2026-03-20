package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.MeanCalculation;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.result.MeanResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.util.DecimalUtils.OUTPUT_SCALE;

@Service
public class MeanCalculationServiceImpl extends PeriodAbstractService<MeanResult, PeriodCommand> {

  public MeanCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  public MeanCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    return new MeanCalculation(inputDTO, defaultPeriods).setScale(OUTPUT_SCALE);
  }

}
