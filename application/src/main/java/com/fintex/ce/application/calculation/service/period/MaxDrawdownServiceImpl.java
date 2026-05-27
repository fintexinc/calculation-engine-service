package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MaxDrawdownCalculation;
import com.fintex.ce.application.calculation.service.GrowthOf10KCalculationServiceImpl;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MaxDrawdownServiceImpl extends WeightedAverageWithCpedAbstractService<PeriodCommand, MaxDrawdownResult> {

  public MaxDrawdownServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MAX_DRAWDOWN;
  }

  public MaxDrawdownCalculation defineCalculationMethod(PeriodCommand command) {
    PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    // weightedAveragePortfolioReturns is already in factor form (WeightedAverageComponent applied SCALE_OF_TWO
    // above), so we pass AS_IS to avoid double-scaling 1.05 into 1.0105 and flattening every drawdown.
    var growth10K = GrowthOf10KCalculationServiceImpl.compoundGrowth10K(context.getWeightedAveragePortfolioReturns(),
        ReturnFactorScale.AS_IS);
    return new MaxDrawdownCalculation(context, defaultPeriods, growth10K, DecimalUtils::toUserScale);
  }
}
