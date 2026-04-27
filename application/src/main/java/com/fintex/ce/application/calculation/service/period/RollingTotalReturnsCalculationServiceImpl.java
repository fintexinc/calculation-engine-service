package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;

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
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_TOTAL_RETURNS;
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
    final ReturnsAggregate monthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
  }

}
