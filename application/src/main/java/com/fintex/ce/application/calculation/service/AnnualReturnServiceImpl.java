package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.AnnualReturnCalculation;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.command.ReturnCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class AnnualReturnServiceImpl implements CalculationService<ReturnCommand, AnnualReturnResult<Integer>> {

  private final MonthlyReturnsService monthlyReturnsService;

  @Autowired
  public AnnualReturnServiceImpl(final MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ANNUAL_RETURNS;
  }

  @Override
  public AnnualReturnResult<Integer> perform(final ReturnCommand command) {
    final PeriodCalculationInput context = buildWeightedAverageInput(command);
    return buildAnnualReturnCalculation(context).calculate();
  }

  public AnnualReturnCalculation buildAnnualReturnCalculation(final PeriodCalculationInput context) {
    return new AnnualReturnCalculation(context.getWeightedAveragePortfolioReturns(), context.getWarnings());
  }

  public PeriodCalculationInput buildWeightedAverageInput(final ReturnCommand command) {
    final ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate = monthlyReturnsService
        .getPortfolioMonthlyReturns(command
            .getHoldings(), command.getCurrency(), SCALE_OF_TWO);

    final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, command.getCustomPsd(), command
            .getCustomPed());

    return new PeriodCalculationInput().setWeightedAveragePortfolioReturns(weightedAveragePortfolioReturns)
        .setWarnings(
            monthlyReturnsAggregate.getErrorsAsWarnings());
  }

}
