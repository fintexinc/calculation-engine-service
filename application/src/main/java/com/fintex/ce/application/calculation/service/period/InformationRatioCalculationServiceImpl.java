package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.InformationRatioCalculation;
import com.fintex.ce.application.calculation.metric.TrackingErrorCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class InformationRatioCalculationServiceImpl
    extends
      PeriodBenchmarkAbstractService<InformationRatioResult, PeriodCommand> {

  public InformationRatioCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.information-ratio-returns}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.INFORMATION_RATIO;
  }

  @Override
  public PeriodCalculationAbstract<InformationRatioResult, ?> defineCalculationMethod(PeriodCommand command) {
    final BenchmarkPeriodCalculationInput input = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO);
    final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(input, Set.of());
    final var trackingErrorCalculation = new TrackingErrorCalculation(input, Set.of());
    return new InformationRatioCalculation(input, defaultPeriods, trailingTotalReturnsCalculation,
        trackingErrorCalculation);
  }
}
