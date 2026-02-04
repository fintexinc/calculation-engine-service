package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.ExcessReturnsCalculation;
import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.application.result.ExcessReturnsResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ExcessReturnsCalculationServiceImpl
    extends
      PeriodBenchmarkAbstractService<ExcessReturnsResult, PeriodCommand> {

  public ExcessReturnsCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public PeriodCalculationAbstract<ExcessReturnsResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    return new ExcessReturnsCalculation(inDTO, defaultPeriods);
  }

}
