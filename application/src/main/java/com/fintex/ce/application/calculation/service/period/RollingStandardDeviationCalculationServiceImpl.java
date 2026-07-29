package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingStandardDeviationCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.rolling.RollingStandardDeviationResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.util.Set;

import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class RollingStandardDeviationCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<RollingCalculationCommand, RollingStandardDeviationResult> {

  private final Set<TimePeriod> defaultPeriods;

  public RollingStandardDeviationCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.defaultPeriods = periods.getRollingCalculations();
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_STANDARD_DEVIATION;
  }

  @Override
  public RollingStandardDeviationResult perform(RollingCalculationCommand command,
      PortfolioBenchmarkReturns returnsData) {
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO,
        returnsData);
    StandardDeviationCalculation<PeriodResult> standardDeviationCalculation = StandardDeviationCalculation
        .<PeriodResult>builder()
        .input(input)
        .defaultPeriods(defaultPeriods)
        .scale(OUTPUT_SCALE)
        .build();
    return new RollingStandardDeviationCalculation(input, defaultPeriods, standardDeviationCalculation)
        .calculate(command.getRollingPeriods());
  }
}