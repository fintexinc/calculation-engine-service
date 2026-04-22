package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DistributionOfReturnsCalculation;
import com.fintex.ce.application.calculation.metric.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.util.ReturnFactorScale;

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
  public DistributionOfReturnsResult perform(final DistributionOfReturnsCommand reqDTO) {
    final CalculationDTO inputWithScaleOfOne = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final CalculationDTO inputWithScaleOfTwo = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(inputWithScaleOfTwo, Set.of());
    final var rollingTotalReturnsCalculation = new RollingTotalReturnsCalculation(inputWithScaleOfTwo, Set.of(),
        trailingTotalReturnsCalculation);
    return new DistributionOfReturnsCalculation(rollingTotalReturnsCalculation, inputWithScaleOfOne
        .getWeightedAveragePortfolioReturns()).calculate(reqDTO);
  }

  public CalculationDTO buildCalculationDto(final DistributionOfReturnsCommand reqDTO,
      final ReturnFactorScale returnFactorScale) {
    final ReturnsAggregate portfolioMonthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(portfolioMonthlyReturnsAggregate, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
  }
}
