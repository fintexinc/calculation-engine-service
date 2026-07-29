package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MeanCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.MeanResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class MeanCalculationServiceImpl extends WeightedAverageWithCpedAbstractService<PeriodCommand, MeanResult> {

  public MeanCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, periods.getRiskCalculations());
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MEAN;
  }

  @Override
  public MeanResult perform(final PeriodCommand command,
      final PortfolioBenchmarkReturns returnsData) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO,
        returnsData);
    return MeanCalculation.<MeanResult>builder()
        .input(context)
        .defaultPeriods(defaultPeriods)
        .scale(OUTPUT_SCALE)
        .build()
        .calculate(command.getPeriods());
  }

}
