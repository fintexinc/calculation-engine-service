package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.BestWorstPeriodCalculation;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.period.BestWorstPeriodsResult;
import com.fintex.ce.model.dto.command.BestWorstPeriodsCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class BestWorstPeriodsCalculationServiceImpl
    implements
      CalculationService<BestWorstPeriodsCommand, BestWorstPeriodsResult> {

  private final MonthlyReturnsService monthlyReturnsService;

  @Value("#{'${default.periods.best-worst-periods}'.split(',')}")
  public Set<Long> defaultPeriods;

  @Autowired
  public BestWorstPeriodsCalculationServiceImpl(final MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.BEST_WORST_PERIODS;
  }

  @Override
  public BestWorstPeriodsResult perform(final BestWorstPeriodsCommand command) {
    final PeriodCalculationInput context = buildWeightedAverageInput(command);
    return buildBestWorstPeriodCalculation(command, context).calculate();
  }

  public BestWorstPeriodCalculation buildBestWorstPeriodCalculation(BestWorstPeriodsCommand command,
      PeriodCalculationInput context) {
    return new BestWorstPeriodCalculation(context.getWeightedAveragePortfolioReturns(), getPeriods(command));
  }

  public PeriodCalculationInput buildWeightedAverageInput(final BestWorstPeriodsCommand command) {
    final ReturnsAggregate monthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(command
        .getHoldings(), command
            .getCurrency(), SCALE_OF_TWO);

    final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, command.getCustomPsd(), command
            .getCustomPed());

    return new PeriodCalculationInput(weightedAveragePortfolioReturns);
  }

  public Set<Long> getPeriods(final BestWorstPeriodsCommand command) {
    return !isEmpty(command.getBestWorstTimeIntervalPeriods())
        ? command.getBestWorstTimeIntervalPeriods()
        : defaultPeriods;
  }

}
