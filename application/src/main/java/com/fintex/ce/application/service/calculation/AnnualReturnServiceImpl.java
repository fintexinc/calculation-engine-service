package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.calculation.AnnualReturnCalculation;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.ReturnCommand;
import com.fintex.ce.port.input.result.AnnualReturnResult;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.service.calculation.CalculationService;
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
  public AnnualReturnResult<Integer> perform(final ReturnCommand reqDTO) {
    final CalculationDTO inputDTO = buildWeightedAverageInputDto(reqDTO);
    return buildAnnualReturnCalculation(inputDTO).calculate();
  }

  public AnnualReturnCalculation buildAnnualReturnCalculation(final CalculationDTO inputDTO) {
    return new AnnualReturnCalculation(inputDTO.getWeightedAveragePortfolioReturns(), inputDTO.getWarnings());
  }

  public CalculationDTO buildWeightedAverageInputDto(final ReturnCommand reqDTO) {
    final Returns<MonthlyReturns> monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO
        .getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

    final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPerformanceStartDate(), reqDTO
            .getCustomPerformanceEndDate());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(weightedAveragePortfolioReturns).setWarnings(
        monthlyReturns.getErrorsAsWarnings());
  }

}
