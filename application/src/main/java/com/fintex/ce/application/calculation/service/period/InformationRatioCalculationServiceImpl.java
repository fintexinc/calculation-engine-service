package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.InformationRatioCalculation;
import com.fintex.ce.application.calculation.metric.TrackingErrorCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
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
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class InformationRatioCalculationServiceImpl
    extends
      BenchmarkWeightedAverageWithCpedAbstractService<PeriodCommand, InformationRatioResult> {

  public InformationRatioCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      BenchmarkWeightedAverageWithCpedPipeline benchmarkWeightedAverageWithCped,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, benchmarkMonthlyReturnsContextProvider,
        portfolioWeightedAverageWithCped, benchmarkWeightedAverageWithCped, periods.getInformationRatioReturns());
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.INFORMATION_RATIO;
  }

  @Override
  public InformationRatioResult perform(PeriodCommand command,
      PortfolioBenchmarkReturns returnsData) {
    final BenchmarkPeriodCalculationInput input = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO, returnsData);
    final var trailingTotalReturnsCalculation = TrailingTotalReturnsCalculation.mathOnly(input, Set.of());
    final var trackingErrorCalculation = new TrackingErrorCalculation(input, Set.of());
    return new InformationRatioCalculation(input, defaultPeriods, trailingTotalReturnsCalculation,
        trackingErrorCalculation).calculate(command.getPeriods());
  }
}
