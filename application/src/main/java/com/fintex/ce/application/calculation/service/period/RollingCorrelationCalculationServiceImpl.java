package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.metric.RollingCorrelationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.rolling.RollingCorrelationResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.PceExceptionCollector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class RollingCorrelationCalculationServiceImpl
    extends
      PeriodBenchmarkAbstractService<RollingCorrelationResult, RollingCalculationCommand> {

  public RollingCorrelationCalculationServiceImpl(
      MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_CORRELATION;
  }

  @Override
  public RollingCorrelationResult perform(RollingCalculationCommand command) {
    RollingCorrelationCalculation rollingCorrelationCalculation = defineCalculationMethod(command);
    return rollingCorrelationCalculation.calculate(command.getRollingPeriods());
  }

  @Override
  public RollingCorrelationCalculation defineCalculationMethod(RollingCalculationCommand command) {
    BenchmarkPeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    Map<PortfolioHolding, Map<LocalDate, BigDecimal>> baseTotalReturn = getBaseTotalReturns(command);
    var correlationCalculation = new CorrelationCalculation(context, baseTotalReturn, defaultPeriods);
    return new RollingCorrelationCalculation(context, defaultPeriods, correlationCalculation, context
        .getWeightedAverageBenchmarkReturns());
  }

  public Map<PortfolioHolding, Map<LocalDate, BigDecimal>> getBaseTotalReturns(RollingCalculationCommand command) {
    ReturnsAggregate monthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(command.getHoldings(),
        command
            .getCurrency(), ReturnFactorScale.SCALE_OF_TWO);

    return monthlyReturnsAggregate
        .validateCped(command.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(command.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap();
  }

  @Override
  public BenchmarkPeriodCalculationInput buildPeriodCalculationInput(RollingCalculationCommand command,
      ReturnFactorScale returnFactorScale) {
    PceExceptionCollector notification = new PceExceptionCollector();

    ReturnsAggregate portfolioMonthlyReturnsAggregate = notification.tryCatch(() -> monthlyReturnsService
        .getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency(), returnFactorScale));
    ReturnsAggregate benchmarkMonthlyReturnsAggregate = notification.tryCatch(() -> monthlyReturnsService
        .getBenchmarkMonthlyReturns(command.getBenchmarkHoldings(), command.getCurrency(), returnFactorScale));
    notification.throwIfAny();

    portfolioMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(benchmarkMonthlyReturnsAggregate);
    benchmarkMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(portfolioMonthlyReturnsAggregate);

    NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(portfolioMonthlyReturnsAggregate, command.getCustomPsd(), command
            .getCustomPed()));
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(benchmarkMonthlyReturnsAggregate, command.getCustomPsd(), command
            .getCustomPed()));
    notification.throwIfAny();

    var result = new BenchmarkPeriodCalculationInput();
    result.setWeightedAverageBenchmarkReturns(benchmarkTotalReturns);
    result.setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    result.setCipsd(command.getCustomIntervalPsd());
    return result;
  }

}
