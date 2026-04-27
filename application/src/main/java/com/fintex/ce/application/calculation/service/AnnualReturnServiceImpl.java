package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.AnnualReturnCalculation;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
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
  public AnnualReturnResult<Integer> perform(final ReturnCommand reqDTO) {
    final CalculationDTO inputDTO = buildWeightedAverageInputDto(reqDTO);
    return buildAnnualReturnCalculation(inputDTO).calculate();
  }

  public AnnualReturnCalculation buildAnnualReturnCalculation(final CalculationDTO inputDTO) {
    return new AnnualReturnCalculation(inputDTO.getWeightedAveragePortfolioReturns(), inputDTO.getWarnings());
  }

  public CalculationDTO buildWeightedAverageInputDto(final ReturnCommand reqDTO) {
    final ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate = monthlyReturnsService
        .getPortfolioMonthlyReturns(reqDTO
            .getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

    final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(weightedAveragePortfolioReturns).setWarnings(
        monthlyReturnsAggregate.getErrorsAsWarnings());
  }

}
