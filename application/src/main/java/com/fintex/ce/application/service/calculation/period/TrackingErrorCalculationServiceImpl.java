package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.TrackingErrorCalculation;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.application.result.TrackingErrorResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TrackingErrorCalculationServiceImpl
    extends
      PeriodBenchmarkAbstractService<TrackingErrorResult, PeriodCommand> {

  public TrackingErrorCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public PeriodCalculationAbstract<TrackingErrorResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    return new TrackingErrorCalculation(input, defaultPeriods);
  }
}
