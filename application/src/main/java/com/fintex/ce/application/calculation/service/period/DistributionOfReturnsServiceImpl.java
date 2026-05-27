package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DistributionOfReturnsCalculation;
import com.fintex.ce.application.calculation.metric.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DistributionOfReturnsServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<DistributionOfReturnsCommand, DistributionOfReturnsResult> {

  public DistributionOfReturnsServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS;
  }

  @Override
  public DistributionOfReturnsResult perform(DistributionOfReturnsCommand command) {
    PeriodCalculationInput inputWithScaleOfOne = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE);
    PeriodCalculationInput inputWithScaleOfTwo = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    TrailingTotalReturnsCalculation trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(
        inputWithScaleOfTwo, Set.of());
    RollingTotalReturnsCalculation rollingTotalReturnsCalculation = new RollingTotalReturnsCalculation(
        inputWithScaleOfTwo, Set.of(), trailingTotalReturnsCalculation);
    return new DistributionOfReturnsCalculation(rollingTotalReturnsCalculation, inputWithScaleOfOne
        .getWeightedAveragePortfolioReturns()).calculate(command);
  }
}