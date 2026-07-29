package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.service.period.core.ValidateCutAndFxAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.returns.pipeline.PortfolioValidateCutAndFxPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.CorrelationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class CorrelationServiceImpl extends ValidateCutAndFxAbstractService<PeriodCommand, CorrelationResult> {

  private final Set<TimePeriod> defaultPeriods;

  public CorrelationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioValidateCutAndFxPipeline portfolioValidateCutAndFx,
      WeightedAverageComponent weightedAverageComponent,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioValidateCutAndFx, weightedAverageComponent);
    this.defaultPeriods = periods.getRiskCalculations();
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.CORRELATION;
  }

  @Override
  public CorrelationResult perform(PeriodCommand command, PortfolioBenchmarkReturns returnsData) {
    command.setReqCurrencyToCashHolding();
    ReturnsSnapshot<HoldingMonthlyReturns> postFx = runValidateCutAndFx(command, returnsData);
    Map<PortfolioHolding, Map<LocalDate, BigDecimal>> baseTotalReturns = new HashMap<>(postFx.returnsMap());

    NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = weightedAverageAfterPsdTrim(postFx,
        ReturnFactorScale.SCALE_OF_TWO);

    PeriodCalculationInput context = new PeriodCalculationInput(command.getCustomIntervalPsd(),
        weightedAveragePortfolioReturns);
    return new CorrelationCalculation(context, baseTotalReturns, defaultPeriods).calculate(command.getPeriods());
  }
}