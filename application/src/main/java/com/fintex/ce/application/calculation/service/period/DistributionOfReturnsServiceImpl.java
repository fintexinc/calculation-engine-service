package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DistributionOfReturnsCalculation;
import com.fintex.ce.application.calculation.metric.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class DistributionOfReturnsServiceImpl
    implements
      CalculationService<DistributionOfReturnsCommand, DistributionOfReturnsResult> {

  private final MonthlyReturnsService monthlyReturnsService;

  public DistributionOfReturnsServiceImpl(final MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS;
  }

  @Override
  public DistributionOfReturnsResult perform(final DistributionOfReturnsCommand command) {
    final PeriodCalculationInput inputWithScaleOfOne = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_ONE);
    final PeriodCalculationInput inputWithScaleOfTwo = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO);
    final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(inputWithScaleOfTwo, Set.of());
    final var rollingTotalReturnsCalculation = new RollingTotalReturnsCalculation(inputWithScaleOfTwo, Set.of(),
        trailingTotalReturnsCalculation);
    return new DistributionOfReturnsCalculation(rollingTotalReturnsCalculation, inputWithScaleOfOne
        .getWeightedAveragePortfolioReturns()).calculate(command);
  }

  public PeriodCalculationInput buildPeriodCalculationInput(final DistributionOfReturnsCommand command,
      final ReturnFactorScale returnFactorScale) {
    final ReturnsAggregate portfolioMonthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(portfolioMonthlyReturnsAggregate, command.getCustomPsd(), command
            .getCustomPed());

    return new PeriodCalculationInput(portfolioTotalReturns);
  }
}
