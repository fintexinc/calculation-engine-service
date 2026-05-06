package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.LeadingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.LeadingTotalReturnsResult;
import com.fintex.ce.model.dto.command.LeadingTotalReturnCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LeadingTotalReturnsCalculationServiceImpl
    extends
      PeriodAbstractService<LeadingTotalReturnsResult, LeadingTotalReturnCommand> {

  public LeadingTotalReturnsCalculationServiceImpl(
      @Autowired MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.leading-total-returns}'.split(',')}") Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.LEADING_TOTAL_RETURNS;
  }

  @Override
  public LeadingTotalReturnsResult perform(LeadingTotalReturnCommand command) {
    LeadingTotalReturnsCalculation leadingTotalReturnsCalculation = defineCalculationMethod(command);
    return leadingTotalReturnsCalculation.calculate(command.getPeriods());
  }

  @Override
  public LeadingTotalReturnsCalculation defineCalculationMethod(LeadingTotalReturnCommand command) {
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    return new LeadingTotalReturnsCalculation(input, defaultPeriods);
  }

  @Override
  public PeriodCalculationInput buildPeriodCalculationInput(LeadingTotalReturnCommand command,
      ReturnFactorScale returnFactorScale) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency());
    WeightedAverageResult<HoldingMonthlyReturns> result = monthlyReturnsService
        .calculateWeightedAverageWithCpsdAndCped(portfolioContext, command.getCustomPsd(), null, returnFactorScale);
    return new PeriodCalculationInput(result.weightedAverage());
  }
}
