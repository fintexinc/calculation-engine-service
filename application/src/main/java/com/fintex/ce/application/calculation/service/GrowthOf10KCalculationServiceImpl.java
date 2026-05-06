package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.dto.command.ReturnCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class GrowthOf10KCalculationServiceImpl implements CalculationService<ReturnCommand, Growth10KResult> {

  private final MonthlyReturnsService monthlyReturnsService;

  @Autowired
  public GrowthOf10KCalculationServiceImpl(MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.GROWTH_OF_10K;
  }

  @Override
  public Growth10KResult perform(ReturnCommand command) {
    PeriodCalculationInput context = buildPeriodCalculationInput(command);
    return buildGrowth10kCalculation(command, context).calculate();
  }

  public Growth10KCalculation buildGrowth10kCalculation(ReturnCommand command, PeriodCalculationInput context) {
    return new Growth10KCalculation(
        context.getWeightedAveragePortfolioReturns(),
        new DateRange(command.getCustomPsd(), command.getCustomPed()),
        false,
        context.getWarnings());
  }

  public PeriodCalculationInput buildPeriodCalculationInput(ReturnCommand command) {
    MonthlyReturnsContext<HoldingMonthlyReturns> monthlyReturnsContext = monthlyReturnsService
        .getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency());
    WeightedAverageResult<HoldingMonthlyReturns> result = monthlyReturnsService
        .calculateWeightedAverageWithCpsdAndCped(monthlyReturnsContext, command.getCustomPsd(), command.getCustomPed(),
            SCALE_OF_TWO);
    return PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(result.weightedAverage())
        .warnings(result.getErrorsAsWarnings())
        .build();
  }
}
