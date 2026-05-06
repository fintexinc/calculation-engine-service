package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.AnnualReturnCalculation;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.command.ReturnCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class AnnualReturnServiceImpl implements CalculationService<ReturnCommand, AnnualReturnResult<Integer>> {

  private final MonthlyReturnsService monthlyReturnsService;

  @Autowired
  public AnnualReturnServiceImpl(MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ANNUAL_RETURNS;
  }

  @Override
  public AnnualReturnResult<Integer> perform(ReturnCommand command) {
    PeriodCalculationInput context = buildWeightedAverageInput(command);
    return buildAnnualReturnCalculation(context).calculate();
  }

  public AnnualReturnCalculation buildAnnualReturnCalculation(PeriodCalculationInput context) {
    return new AnnualReturnCalculation(context.getWeightedAveragePortfolioReturns(), context.getWarnings());
  }

  public PeriodCalculationInput buildWeightedAverageInput(ReturnCommand command) {
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
