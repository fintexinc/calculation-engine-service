package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrackingErrorCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.period.core.BenchmarkWeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.TrackingErrorResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TrackingErrorCalculationServiceImpl
    extends
      BenchmarkWeightedAverageWithCpedAbstractService<PeriodCommand, TrackingErrorResult> {

  public TrackingErrorCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      BenchmarkWeightedAverageWithCpedPipeline benchmarkWeightedAverageWithCped,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, benchmarkMonthlyReturnsContextProvider,
        portfolioWeightedAverageWithCped, benchmarkWeightedAverageWithCped, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TRACKING_ERROR;
  }

  @Override
  public PeriodCalculationAbstract<TrackingErrorResult, ?> defineCalculationMethod(final PeriodCommand command) {
    final BenchmarkPeriodCalculationInput input = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO);
    return new TrackingErrorCalculation(input, defaultPeriods);
  }
}
