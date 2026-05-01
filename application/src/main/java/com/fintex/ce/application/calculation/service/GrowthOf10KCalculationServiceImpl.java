package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.dto.command.ReturnCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class GrowthOf10KCalculationServiceImpl implements CalculationService<ReturnCommand, Growth10KResult> {

  private final MonthlyReturnsService monthlyReturnsService;

  @Autowired
  public GrowthOf10KCalculationServiceImpl(final MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.GROWTH_OF_10K;
  }

  @Override
  public Growth10KResult perform(final ReturnCommand command) {
    return calculateDefaultGrowthOf10K(command);
  }

  private Growth10KResult calculateDefaultGrowthOf10K(final ReturnCommand command) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command);
    Growth10KCalculation growth10KCalculation = buildGrowth10kCalculation(command, context);
    return growth10KCalculation.calculate();
  }

  public Growth10KCalculation buildGrowth10kCalculation(ReturnCommand command, PeriodCalculationInput context) {
    return new Growth10KCalculation(
        context.getWeightedAveragePortfolioReturns(),
        new DateRange(command.getCustomPsd(), command.getCustomPed()),
        false,
        context.getWarnings());
  }

  public PeriodCalculationInput buildPeriodCalculationInput(final ReturnCommand command) {
    final ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate = monthlyReturnsService
        .getPortfolioMonthlyReturns(command
            .getHoldings(), command.getCurrency(), SCALE_OF_TWO);

    monthlyReturnsAggregate
        .setCpedDataValidation(new PortfolioCpedDataValidation())
        .setCpsdDataValidation(new PortfolioCpsdDataValidation());

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, command.getCustomPsd(), command
            .getCustomPed());

    return PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(portfolioTotalReturns)
        .warnings(monthlyReturnsAggregate
            .getErrorsAsWarnings())
        .build();
  }
}
