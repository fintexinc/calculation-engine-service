package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.UpsideCaptureCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.result.UpsideCaptureResult;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
  public CalculationMetric getMetric() {
    return CalculationMetric.UPSIDE_CAPTURE;
  }

  @Override
  public PeriodCalculationAbstract<UpsideCaptureResult, ?> defineCalculationMethod(final PeriodCommand reqDTO) {
    final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, AS_IS);
    return new UpsideCaptureCalculation(inDTO, defaultPeriods);
  }

}
