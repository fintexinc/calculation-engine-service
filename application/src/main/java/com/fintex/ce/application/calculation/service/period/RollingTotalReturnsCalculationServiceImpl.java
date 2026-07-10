package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class RollingTotalReturnsCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<RollingCalculationCommand, RollingTotalReturnsResult> {

  private final Set<String> defaultPeriods;

  public RollingTotalReturnsCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.defaultPeriods = defaultPeriods;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_TOTAL_RETURNS;
  }

  @Override
  public RollingTotalReturnsResult perform(RollingCalculationCommand command) {
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    TrailingTotalReturnsCalculation trailingTotalReturnsCalculation = TrailingTotalReturnsCalculation.mathOnly(input,
        defaultPeriods);
    return new RollingTotalReturnsCalculation(input, defaultPeriods, trailingTotalReturnsCalculation)
        .calculate(command.getRollingPeriods());
  }
}