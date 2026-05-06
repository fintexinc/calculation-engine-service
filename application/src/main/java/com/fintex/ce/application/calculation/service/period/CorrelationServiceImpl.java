package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
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
public class CorrelationServiceImpl extends PeriodAbstractService<CorrelationResult, PeriodCommand> {

  public CorrelationServiceImpl(
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") Set<String> defaultPeriods,
      MonthlyReturnsService monthlyReturnsService) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.CORRELATION;
  }

  @Override
  public CorrelationResult perform(PeriodCommand command) {
    PeriodCalculationAbstract<CorrelationResult, ?> calculationMethod = defineCalculationMethod(command);
    return calculationMethod.calculate(command.getPeriods());
  }

  public CorrelationCalculation defineCalculationMethod(PeriodCommand command) {
    command.setReqCurrencyToCashHolding();
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency());

    ReturnsSnapshot<HoldingMonthlyReturns> postFx = monthlyReturnsService.applyValidateCutAndFx(portfolioContext,
        command.getCustomPed());
    Map<PortfolioHolding, Map<LocalDate, BigDecimal>> baseTotalReturns = new HashMap<>(postFx.returnsMap());

    NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
        .calculateWeightedAverageAfterPsdTrim(postFx, ReturnFactorScale.SCALE_OF_TWO);

    PeriodCalculationInput context = new PeriodCalculationInput(command.getCustomIntervalPsd(),
        weightedAveragePortfolioReturns);
    return new CorrelationCalculation(context, baseTotalReturns, defaultPeriods);
  }
}
