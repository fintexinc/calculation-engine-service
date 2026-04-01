package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.ReturnCommand;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.result.Growth10KResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class GrowthOf10KCalculationServiceImpl implements CalculationService<Growth10KResult, ReturnCommand> {

  private final MonthlyReturnsService monthlyReturnsService;

  @Autowired
  public GrowthOf10KCalculationServiceImpl(final MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
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
        new CommonDates(reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate()),
        false,
        inputDTO.getWarnings());
  }

  public CalculationDTO buildCalculationDto(final ReturnCommand reqDTO) {
    final Returns<HoldingMonthlyReturns> monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO
        .getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

    monthlyReturns
        .setCpedDataValidation(new PortfolioCpedDataValidation())
        .setCpsdDataValidation(new PortfolioCpsdDataValidation());

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPerformanceStartDate(), reqDTO
            .getCustomPerformanceEndDate());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns).setWarnings(monthlyReturns
        .getErrorsAsWarnings());
  }
}
