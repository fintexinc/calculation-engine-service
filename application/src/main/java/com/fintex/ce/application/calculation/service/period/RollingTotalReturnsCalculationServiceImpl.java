package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RollingTotalReturnsCalculationServiceImpl
    extends
      PeriodAbstractService<RollingTotalReturnsResult, RollingCalculationCommand> {

  public RollingTotalReturnsCalculationServiceImpl(
      MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_TOTAL_RETURNS;
  }

  @Override
  public RollingTotalReturnsResult perform(RollingCalculationCommand command) {
    RollingTotalReturnsCalculation rollingTotalReturnsCalculation = defineCalculationMethod(command);
    return rollingTotalReturnsCalculation.calculate(command.getRollingPeriods());
  }

  @Override
  public RollingTotalReturnsCalculation defineCalculationMethod(RollingCalculationCommand command) {
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    TrailingTotalReturnsCalculation trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(input,
        defaultPeriods);
    return new RollingTotalReturnsCalculation(input, defaultPeriods, trailingTotalReturnsCalculation);
  }

  @Override
  public PeriodCalculationInput buildPeriodCalculationInput(RollingCalculationCommand command,
      ReturnFactorScale returnFactorScale) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency());
    WeightedAverageResult<HoldingMonthlyReturns> result = monthlyReturnsService
        .calculateWeightedAverageWithCpsdAndCped(portfolioContext, command.getCustomPsd(), command.getCustomPed(),
            returnFactorScale);
    return new PeriodCalculationInput(result.weightedAverage());
  }
}
