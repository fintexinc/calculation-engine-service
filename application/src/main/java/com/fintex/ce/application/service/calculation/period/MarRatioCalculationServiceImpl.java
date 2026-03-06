package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.Growth10KCalculation;
import com.fintex.ce.application.calculation.MarRatioCalculation;
import com.fintex.ce.application.calculation.MaxDrawdownCalculation;
import com.fintex.ce.application.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.result.MARRatioResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
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
public class MarRatioCalculationServiceImpl extends PeriodAbstractService<MARRatioResult, PeriodCommand> {

  public static final Function<BigDecimal, BigDecimal> SCALE_FUNCTION = e -> e;

  public MarRatioCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public MARRatioResult perform(final PeriodCommand reqDTO) {
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
