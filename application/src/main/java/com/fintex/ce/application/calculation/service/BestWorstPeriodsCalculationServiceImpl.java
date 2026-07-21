package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.BestWorstPeriodCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.period.BestWorstPeriodsResult;
import com.fintex.ce.model.dto.command.BestWorstPeriodsCommand;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class BestWorstPeriodsCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<BestWorstPeriodsCommand, BestWorstPeriodsResult> {

  @Value("#{'${default.periods.best-worst-periods}'.split(',')}")
  public Set<Long> defaultPeriods;

  public BestWorstPeriodsCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.BEST_WORST_PERIODS;
  }

  @Override
  public BestWorstPeriodsResult perform(BestWorstPeriodsCommand command,
      PortfolioBenchmarkReturns returnsData) {
    PeriodCalculationInput context = buildPeriodCalculationInput(command, SCALE_OF_TWO, returnsData);
    return new BestWorstPeriodCalculation(context.getWeightedAveragePortfolioReturns(), getPeriods(command))
        .calculate();
  }

  public Set<Long> getPeriods(BestWorstPeriodsCommand command) {
    return !isEmpty(command.getBestWorstTimeIntervalPeriods())
        ? command.getBestWorstTimeIntervalPeriods()
        : defaultPeriods;
  }
}