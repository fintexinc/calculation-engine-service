package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.calculation.metric.MarRatioCalculation;
import com.fintex.ce.application.calculation.metric.MaxDrawdownCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.util.ReturnFactorScale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

@Service
public class MarRatioCalculationServiceImpl extends PeriodAbstractService<MarRatioResult, PeriodCommand> {

  public static final Function<BigDecimal, BigDecimal> SCALE_FUNCTION = e -> e;

  public MarRatioCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MAR_RATIO;
  }

  @Override
  public MarRatioResult perform(final PeriodCommand reqDTO) {
    final MarRatioCalculation calculationMethod = defineCalculationMethod(reqDTO);
    return calculationMethod.calculate(reqDTO.getPeriods());
  }

  public MarRatioCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(inputDTO, defaultPeriods);

    final var growth10KCalculation = new Growth10KCalculation(inputDTO.getWeightedAveragePortfolioReturns(), null,
        false);
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    growth10KCalculation.setFirstGrowth10KValue(inputDTO.getWeightedAveragePortfolioReturns(), growth10K);
    growth10KCalculation.calculateGrowth10K(inputDTO.getWeightedAveragePortfolioReturns(), growth10K);

    final var maxDrawdownCalculation = new MaxDrawdownCalculation(inputDTO, defaultPeriods, growth10K, SCALE_FUNCTION);
    return new MarRatioCalculation(inputDTO, defaultPeriods, trailingTotalReturnsCalculation, maxDrawdownCalculation);
  }

}
