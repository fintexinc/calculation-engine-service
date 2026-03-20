package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.DistributionOfReturnsCalculation;
import com.fintex.ce.application.calculation.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.domain.model.result.DistributionOfReturnsResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.service.calculation.CalculationService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class DistributionOfReturnsServiceImpl
    implements
      CalculationService<DistributionOfReturnsResult, DistributionOfReturnsCommand> {

  private final MonthlyReturnsService monthlyReturnsService;

  public DistributionOfReturnsServiceImpl(final MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
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
    final Returns portfolioMonthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(portfolioMonthlyReturns, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
  }
}
