package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.ExcessReturnsCalculation;
import com.fintex.ce.application.calculation.service.period.core.BenchmarkWeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.ExcessReturnsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

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
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, benchmarkMonthlyReturnsContextProvider,
        portfolioWeightedAverageWithCped, benchmarkWeightedAverageWithCped, periods.getRiskCalculations());
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EXCESS_RETURNS;
  }

  @Override
  public ExcessReturnsResult perform(final PeriodCommand command,
      final PortfolioBenchmarkReturns returnsData) {
    final BenchmarkPeriodCalculationInput context = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO, returnsData);
    return new ExcessReturnsCalculation(context, defaultPeriods).calculate(command.getPeriods());
  }

}
