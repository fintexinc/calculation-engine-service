package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class RollingTotalReturnsCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<RollingCalculationCommand, RollingTotalReturnsResult> {

  private final Set<TimePeriod> defaultPeriods;

  public RollingTotalReturnsCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.defaultPeriods = periods.getRollingCalculations();
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_TOTAL_RETURNS;
  }

  @Override
  public RollingTotalReturnsResult perform(RollingCalculationCommand command,
      PortfolioBenchmarkReturns returnsData) {
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO,
        returnsData);
    TrailingTotalReturnsCalculation trailingTotalReturnsCalculation = TrailingTotalReturnsCalculation.mathOnly(input,
        defaultPeriods);
    return new RollingTotalReturnsCalculation(input, defaultPeriods, trailingTotalReturnsCalculation)
        .calculate(command.getRollingPeriods());
  }
}
