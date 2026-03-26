package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.calculation.metric.MaxDrawdownCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.result.MaxDrawdownResult;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.ReturnFactorScale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class MaxDrawdownServiceImpl extends PeriodAbstractService<MaxDrawdownResult, PeriodCommand> {

  public MaxDrawdownServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  public MaxDrawdownCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    final var growth10KCalculation = new Growth10KCalculation(inputDTO.getWeightedAveragePortfolioReturns(), null,
        false);
    final NavigableMap<LocalDate, BigDecimal> growth10K = initializeGrowthOf10KMap(inputDTO, growth10KCalculation);
    return new MaxDrawdownCalculation(inputDTO, defaultPeriods, growth10K, DecimalUtils::toUserScale);
  }

  public NavigableMap<LocalDate, BigDecimal> initializeGrowthOf10KMap(final CalculationDTO inputDTO,
      final Growth10KCalculation growth10KCalculation) {
    final NavigableMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    if (!CollectionUtils.isEmpty(inputDTO.getWeightedAveragePortfolioReturns())) {
      growth10KCalculation.setFirstGrowth10KValue(inputDTO.getWeightedAveragePortfolioReturns(), growth10K);
      growth10KCalculation.calculateGrowth10K(inputDTO.getWeightedAveragePortfolioReturns(), growth10K);
    }
    return growth10K;
  }

  @Override
  public void addSpecificChecks(PeriodCommand reqDTO) {
    // There are no specific checks for MaxDrawdownCalculation
  }
}
