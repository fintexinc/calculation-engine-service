package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.UpsideCaptureCalculation;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.application.result.UpsideCaptureResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodBenchmarkAbstractService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.util.ReturnFactorScale.AS_IS;

@Service
public class UpsideCaptureCalculationServiceImpl
    extends
      PeriodBenchmarkAbstractService<UpsideCaptureResult, PeriodCommand> {

  public UpsideCaptureCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public PeriodCalculationAbstract<UpsideCaptureResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, AS_IS);
    return new UpsideCaptureCalculation(inDTO, defaultPeriods);
  }

}
