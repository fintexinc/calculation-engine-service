package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MeanCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.MeanResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;

@Service
public class MeanCalculationServiceImpl extends WeightedAverageWithCpedAbstractService<PeriodCommand, MeanResult> {

  public MeanCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MEAN;
  }

  public MeanCalculation defineCalculationMethod(final PeriodCommand command) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    return MeanCalculation.builder()
        .input(context)
        .defaultPeriods(defaultPeriods)
        .scale(OUTPUT_SCALE)
        .build();
  }

}
