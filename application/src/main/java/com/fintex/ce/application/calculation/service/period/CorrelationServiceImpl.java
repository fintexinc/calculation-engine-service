package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.service.period.core.ValidateCutAndFxAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.returns.pipeline.PortfolioValidateCutAndFxPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.CorrelationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class CorrelationServiceImpl extends ValidateCutAndFxAbstractService<PeriodCommand, CorrelationResult> {

  private final Set<String> defaultPeriods;

  public CorrelationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioValidateCutAndFxPipeline portfolioValidateCutAndFx,
      WeightedAverageComponent weightedAverageComponent,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioValidateCutAndFx, weightedAverageComponent);
    this.defaultPeriods = defaultPeriods;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.CORRELATION;
  }

  @Override
  public CorrelationResult perform(PeriodCommand command) {
    command.setReqCurrencyToCashHolding();
    ReturnsSnapshot<HoldingMonthlyReturns> postFx = runValidateCutAndFx(command);
    Map<PortfolioHolding, Map<LocalDate, BigDecimal>> baseTotalReturns = new HashMap<>(postFx.returnsMap());

    NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = weightedAverageAfterPsdTrim(postFx,
        ReturnFactorScale.SCALE_OF_TWO);

    PeriodCalculationInput context = new PeriodCalculationInput(command.getCustomIntervalPsd(),
        weightedAveragePortfolioReturns);
    return new CorrelationCalculation(context, baseTotalReturns, defaultPeriods).calculate(command.getPeriods());
  }
}