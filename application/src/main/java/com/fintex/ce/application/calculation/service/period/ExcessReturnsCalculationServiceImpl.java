package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.ExcessReturnsCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.period.core.BenchmarkWeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.ExcessReturnsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class ExcessReturnsCalculationServiceImpl
    extends
      BenchmarkWeightedAverageWithCpedAbstractService<PeriodCommand, ExcessReturnsResult> {

  public ExcessReturnsCalculationServiceImpl(
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
    return CalculationMetric.EXCESS_RETURNS;
  }

  @Override
  public PeriodCalculationAbstract<ExcessReturnsResult, ?> defineCalculationMethod(final PeriodCommand command) {
    final BenchmarkPeriodCalculationInput context = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO);
    return new ExcessReturnsCalculation(context, defaultPeriods);
  }

}
