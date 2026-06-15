package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.CpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.calculation.PeriodCalculationService;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import java.util.Set;

/**
 * Base class for services whose pipeline contract is the portfolio-side weighted-average with CPED only (no per-holding
 * CPSD trim). Mirrors {@link PortfolioWeightedAverageWithCpedPipeline} on the service side.
 *
 * @param <C>
 *          command type — must carry portfolio holdings, target currency, and CPED
 * @param <R>
 *          result type produced by the service
 */
public abstract class WeightedAverageWithCpedAbstractService<C extends PeriodCommand, R extends PeriodResult>
    implements
      PeriodCalculationService<C, R> {
  protected final Set<String> defaultPeriods;
  protected final PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider;
  protected final PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped;

  protected WeightedAverageWithCpedAbstractService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      Set<String> defaultPeriods) {
    this.portfolioMonthlyReturnsContextProvider = portfolioMonthlyReturnsContextProvider;
    this.portfolioWeightedAverageWithCped = portfolioWeightedAverageWithCped;
    this.defaultPeriods = defaultPeriods;
  }

  public abstract PeriodCalculationAbstract<R, ?> defineCalculationMethod(C command);

  @Override
  public R perform(C command) {
    PeriodCalculationAbstract<R, ?> calculationMethod = defineCalculationMethod(command);
    return calculationMethod.calculate(command.getPeriods());
  }

  public WeightedAverageResult<HoldingMonthlyReturns> buildWeightedAverageResult(C command,
      ReturnFactorScale scale) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = portfolioMonthlyReturnsContextProvider.get(
        command.getHoldings(), command.getCurrency());
    return portfolioWeightedAverageWithCped.run(portfolioContext, new CpedScaleParams(command.getCustomPed(), scale));
  }

  public PeriodCalculationInput buildPeriodCalculationInput(C command, ReturnFactorScale returnFactorScale) {
    WeightedAverageResult<HoldingMonthlyReturns> result = buildWeightedAverageResult(command, returnFactorScale);
    return new PeriodCalculationInput(command.getCustomIntervalPsd(), result.weightedAverage());
  }
}
