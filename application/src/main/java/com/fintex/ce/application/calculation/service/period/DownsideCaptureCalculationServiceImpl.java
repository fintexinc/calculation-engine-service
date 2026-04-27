package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DownsideCaptureCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.DownsideCaptureResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DownsideCaptureCalculationServiceImpl
    extends
      PeriodBenchmarkAbstractService<DownsideCaptureResult, PeriodCommand> {

  public DownsideCaptureCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.DOWNSIDE_CAPTURE;
  }

  @Override
  public PeriodCalculationAbstract<DownsideCaptureResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.AS_IS);
    return new DownsideCaptureCalculation(inDTO, defaultPeriods);
  }

}
