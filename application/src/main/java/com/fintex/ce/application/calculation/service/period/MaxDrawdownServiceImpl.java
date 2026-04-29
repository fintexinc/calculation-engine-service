package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.calculation.metric.MaxDrawdownCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

@Service
public class MaxDrawdownServiceImpl extends PeriodAbstractService<MaxDrawdownResult, PeriodCommand> {

  public MaxDrawdownServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MAX_DRAWDOWN;
  }

  public MaxDrawdownCalculation defineCalculationMethod(final PeriodCommand command) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    final var growth10KCalculation = new Growth10KCalculation(context.getWeightedAveragePortfolioReturns(), null,
        false);
    final NavigableMap<LocalDate, BigDecimal> growth10K = initializeGrowthOf10KMap(context, growth10KCalculation);
    return new MaxDrawdownCalculation(context, defaultPeriods, growth10K, DecimalUtils::toUserScale);
  }

  public NavigableMap<LocalDate, BigDecimal> initializeGrowthOf10KMap(final PeriodCalculationInput context,
      final Growth10KCalculation growth10KCalculation) {
    final NavigableMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    if (!CollectionUtils.isEmpty(context.getWeightedAveragePortfolioReturns())) {
      growth10KCalculation.setFirstGrowth10KValue(context.getWeightedAveragePortfolioReturns(), growth10K);
      growth10KCalculation.calculateGrowth10K(context.getWeightedAveragePortfolioReturns(), growth10K);
    }
    return growth10K;
  }

  @Override
  public void addSpecificChecks(PeriodCommand command) {
    // There are no specific checks for MaxDrawdownCalculation
  }
}
