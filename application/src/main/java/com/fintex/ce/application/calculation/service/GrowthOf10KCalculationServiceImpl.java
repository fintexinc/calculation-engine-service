package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.ReturnCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;

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
  public Growth10KResult perform(final ReturnCommand reqDTO) {
    return calculateDefaultGrowthOf10K(reqDTO);
  }

  private Growth10KResult calculateDefaultGrowthOf10K(final ReturnCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO);
    Growth10KCalculation growth10KCalculation = buildGrowth10kCalculation(reqDTO, inputDTO);
    return growth10KCalculation.calculate();
  }

  public Growth10KCalculation buildGrowth10kCalculation(ReturnCommand reqDTO, CalculationDTO inputDTO) {
    return new Growth10KCalculation(
        inputDTO.getWeightedAveragePortfolioReturns(),
        new DateRange(reqDTO.getCustomPsd(), reqDTO.getCustomPed()),
        false,
        inputDTO.getWarnings());
  }

  public CalculationDTO buildCalculationDto(final ReturnCommand reqDTO) {
    final ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate = monthlyReturnsService
        .getPortfolioMonthlyReturns(reqDTO
            .getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

    monthlyReturnsAggregate
        .setCpedDataValidation(new PortfolioCpedDataValidation())
        .setCpsdDataValidation(new PortfolioCpsdDataValidation());

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns).setWarnings(
        monthlyReturnsAggregate
            .getErrorsAsWarnings());
  }
}
