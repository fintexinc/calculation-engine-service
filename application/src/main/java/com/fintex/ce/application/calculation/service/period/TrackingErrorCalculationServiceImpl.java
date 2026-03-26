package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrackingErrorCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.result.TrackingErrorResult;
import com.fintex.ce.util.ReturnFactorScale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
