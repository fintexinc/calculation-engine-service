package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.UpsideCaptureCalculation;
import com.fintex.ce.application.calculation.service.period.core.BenchmarkWeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.UpsideCaptureResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

import static com.fintex.ce.application.util.ReturnFactorScale.AS_IS;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class UpsideCaptureCalculationServiceImpl
    extends
      BenchmarkWeightedAverageWithCpedAbstractService<PeriodCommand, UpsideCaptureResult> {

  public UpsideCaptureCalculationServiceImpl(
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
    return CalculationMetric.UPSIDE_CAPTURE;
  }

  @Override
  public UpsideCaptureResult perform(final PeriodCommand command,
      final PortfolioBenchmarkReturns returnsData) {
    final BenchmarkPeriodCalculationInput context = buildPeriodCalculationInput(command, AS_IS, returnsData);
    return new UpsideCaptureCalculation(context, defaultPeriods).calculate(command.getPeriods());
  }

}
