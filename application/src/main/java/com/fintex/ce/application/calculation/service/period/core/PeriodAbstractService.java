package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.calculation.PeriodCalculationService;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import java.util.Set;

/**
 * @param <E>
 *          result object
 */
public abstract class PeriodAbstractService<E extends PeriodResult, R extends PeriodCommand>
    implements
      PeriodCalculationService<R, E> {
  protected final Set<String> defaultPeriods;
  public MonthlyReturnsService monthlyReturnsService;

  protected PeriodAbstractService(MonthlyReturnsService monthlyReturnsService,
      Set<String> defaultPeriods) {
    this.monthlyReturnsService = monthlyReturnsService;
    this.defaultPeriods = defaultPeriods;
  }

  public abstract PeriodCalculationAbstract<E, ?> defineCalculationMethod(R command);

  @Override
  public E perform(R command) {
    PeriodCalculationAbstract<E, ?> calculationMethod = defineCalculationMethod(command);
    return calculationMethod.calculate(command.getPeriods());
  }

  public PeriodCalculationInput buildPeriodCalculationInput(R command, ReturnFactorScale returnFactorScale) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = monthlyReturnsService
        .getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency());
    WeightedAverageResult<HoldingMonthlyReturns> result = monthlyReturnsService
        .calculateWeightedAverageWithCped(portfolioContext, command.getCustomPed(), returnFactorScale);
    return new PeriodCalculationInput(command.getCustomIntervalPsd(), result.weightedAverage());
  }

}
