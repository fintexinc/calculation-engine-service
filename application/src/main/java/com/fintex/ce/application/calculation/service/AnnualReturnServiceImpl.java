package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.AnnualReturnCalculation;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.ReturnCommand;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.result.AnnualReturnResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class AnnualReturnServiceImpl implements CalculationService<AnnualReturnResult<Integer>, ReturnCommand> {

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
  public AnnualReturnResult<Integer> perform(final ReturnCommand reqDTO) {
    final CalculationDTO inputDTO = buildWeightedAverageInputDto(reqDTO);
    return buildAnnualReturnCalculation(inputDTO).calculate();
  }

  public AnnualReturnCalculation buildAnnualReturnCalculation(final CalculationDTO inputDTO) {
    return new AnnualReturnCalculation(inputDTO.getWeightedAveragePortfolioReturns(), inputDTO.getWarnings());
  }

  public CalculationDTO buildWeightedAverageInputDto(final ReturnCommand reqDTO) {
    final Returns<HoldingMonthlyReturns> monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO
        .getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

    final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(weightedAveragePortfolioReturns).setWarnings(
        monthlyReturns.getErrorsAsWarnings());
  }

}
