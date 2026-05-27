package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MarRatioCalculation;
import com.fintex.ce.application.calculation.metric.MaxDrawdownCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.GrowthOf10KCalculationServiceImpl;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Function;

@Service
public class MarRatioCalculationServiceImpl
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, MarRatioResult> {

  public static final Function<BigDecimal, BigDecimal> SCALE_FUNCTION = e -> e;

  public MarRatioCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MAR_RATIO;
  }

  @Override
  public MarRatioResult perform(final PeriodCommand command) {
    final MarRatioCalculation calculationMethod = defineCalculationMethod(command);
    return calculationMethod.calculate(command.getPeriods());
  }

  public MarRatioCalculation defineCalculationMethod(final PeriodCommand command) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(context, defaultPeriods);

    // weightedAveragePortfolioReturns is already in factor form (WeightedAverageComponent applied SCALE_OF_TWO
    // above), so we pass AS_IS to avoid double-scaling 1.05 into 1.0105 and flattening every drawdown.
    final var growth10K = GrowthOf10KCalculationServiceImpl.compoundGrowth10K(context
        .getWeightedAveragePortfolioReturns(),
        ReturnFactorScale.AS_IS);

    final var maxDrawdownCalculation = new MaxDrawdownCalculation(context, defaultPeriods, growth10K, SCALE_FUNCTION);
    return new MarRatioCalculation(context, defaultPeriods, trailingTotalReturnsCalculation, maxDrawdownCalculation);
  }

}
