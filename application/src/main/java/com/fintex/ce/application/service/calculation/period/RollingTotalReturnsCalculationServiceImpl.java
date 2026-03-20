package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.model.result.RollingTotalReturnsResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class RollingTotalReturnsCalculationServiceImpl
    extends
      PeriodAbstractService<RollingTotalReturnsResult, RollingCalculationCommand> {
  public RollingTotalReturnsCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public RollingTotalReturnsResult perform(final RollingCalculationCommand reqDTO) {
    final RollingTotalReturnsCalculation rollingTotalReturnsCalculation = defineCalculationMethod(reqDTO);
    return rollingTotalReturnsCalculation.calculate(reqDTO.getRollingPeriods());
  }

  @Override
  public RollingTotalReturnsCalculation defineCalculationMethod(final RollingCalculationCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(input, defaultPeriods);
    return new RollingTotalReturnsCalculation(input, defaultPeriods, trailingTotalReturnsCalculation);
  }

  @Override
  public CalculationDTO buildCalculationDto(final RollingCalculationCommand reqDTO,
      final ReturnFactorScale returnFactorScale) {
    final Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPsd(), reqDTO.getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
  }

}
