package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.BestWorstPeriodCalculation;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.period.BestWorstPeriodsResult;
import com.fintex.ce.model.dto.command.BestWorstPeriodsCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
  public BestWorstPeriodsCalculationServiceImpl(MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.BEST_WORST_PERIODS;
  }

  @Override
  public BestWorstPeriodsResult perform(BestWorstPeriodsCommand command) {
    PeriodCalculationInput context = buildWeightedAverageInput(command);
    return buildBestWorstPeriodCalculation(command, context).calculate();
  }

  public BestWorstPeriodCalculation buildBestWorstPeriodCalculation(BestWorstPeriodsCommand command,
      PeriodCalculationInput context) {
    return new BestWorstPeriodCalculation(context.getWeightedAveragePortfolioReturns(), getPeriods(command));
  }

  public PeriodCalculationInput buildWeightedAverageInput(BestWorstPeriodsCommand command) {
    MonthlyReturnsContext<HoldingMonthlyReturns> monthlyReturnsContext = monthlyReturnsService
        .getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency());
    WeightedAverageResult<HoldingMonthlyReturns> result = monthlyReturnsService
        .calculateWeightedAverageWithCpsdAndCped(monthlyReturnsContext, command.getCustomPsd(), command.getCustomPed(),
            SCALE_OF_TWO);
    return new PeriodCalculationInput(result.weightedAverage());
  }

  public Set<Long> getPeriods(BestWorstPeriodsCommand command) {
    return !isEmpty(command.getBestWorstTimeIntervalPeriods())
        ? command.getBestWorstTimeIntervalPeriods()
        : defaultPeriods;
  }
}
