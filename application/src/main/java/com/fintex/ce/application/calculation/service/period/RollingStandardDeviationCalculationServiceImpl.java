package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingStandardDeviationCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.rolling.RollingStandardDeviationResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.util.DecimalUtils.OUTPUT_SCALE;

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
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_STANDARD_DEVIATION;
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
    final ReturnsAggregate monthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
  }

}
