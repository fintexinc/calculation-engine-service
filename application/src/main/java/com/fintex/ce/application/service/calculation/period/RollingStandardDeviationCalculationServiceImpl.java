package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.RollingStandardDeviationCalculation;
import com.fintex.ce.application.calculation.StandardDeviationCalculation;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.command.RollingCalculationCommand;
import com.fintex.ce.application.result.RollingStandardDeviationResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.util.DecimalUtils.OUTPUT_SCALE;

@Service
public class RollingStandardDeviationCalculationServiceImpl
    extends
      PeriodAbstractService<RollingStandardDeviationResult, RollingCalculationCommand> {

  public RollingStandardDeviationCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public RollingStandardDeviationResult perform(final RollingCalculationCommand reqDTO) {
    final var rollingStandardDeviationCalculation = defineCalculationMethod(reqDTO);
    return rollingStandardDeviationCalculation.calculate(reqDTO.getRollingPeriods());
  }

  @Override
  public RollingStandardDeviationCalculation defineCalculationMethod(final RollingCalculationCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    final var standardDeviationCalculation = new StandardDeviationCalculation<>(input, defaultPeriods).setScale(
        OUTPUT_SCALE);
    return new RollingStandardDeviationCalculation(input, defaultPeriods, standardDeviationCalculation);
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
