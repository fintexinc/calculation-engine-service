package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.metric.RollingCorrelationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.rolling.RollingCorrelationResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.PceExceptionCollector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
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
    CorrelationCalculation correlationCalculation = new CorrelationCalculation(context, baseTotalReturn,
        defaultPeriods);
    return new RollingCorrelationCalculation(context, defaultPeriods, correlationCalculation,
        context.getWeightedAverageBenchmarkReturns());
  }

  public Map<PortfolioHolding, Map<LocalDate, BigDecimal>> getBaseTotalReturns(RollingCalculationCommand command) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency());
    ReturnsSnapshot<HoldingMonthlyReturns> postFx = monthlyReturnsService.applyValidateCutAndFx(portfolioContext,
        command.getCustomPed());
    return new HashMap<>(postFx.returnsMap());
  }

  @Override
  public BenchmarkPeriodCalculationInput buildPeriodCalculationInput(RollingCalculationCommand command,
      ReturnFactorScale returnFactorScale) {
    PceExceptionCollector collector = new PceExceptionCollector();

    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = collector.tryCatch(
        () -> monthlyReturnsService.getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency()));
    MonthlyReturnsContext<HoldingMonthlyReturns> benchmarkContext = collector.tryCatch(
        () -> monthlyReturnsService.getBenchmarkMonthlyReturns(command.getBenchmarkHoldings(), command.getCurrency()));
    collector.throwIfAny();

    LocalDate commonEnd = monthlyReturnsService.commonPerformanceEndDate(portfolioContext, benchmarkContext);
    MonthlyReturnsContext<HoldingMonthlyReturns> alignedPortfolio = monthlyReturnsService.trimContextToEnd(
        portfolioContext, commonEnd);
    MonthlyReturnsContext<HoldingMonthlyReturns> alignedBenchmark = monthlyReturnsService.trimContextToEnd(
        benchmarkContext, commonEnd);

    WeightedAverageResult<HoldingMonthlyReturns> portfolioResult = collector.tryCatch(
        () -> monthlyReturnsService.calculateWeightedAverageWithCpsdAndCped(alignedPortfolio, command.getCustomPsd(),
            command.getCustomPed(), returnFactorScale));
    WeightedAverageResult<HoldingMonthlyReturns> benchmarkResult = collector.tryCatch(
        () -> monthlyReturnsService.calculateWeightedAverageWithCpsdAndCped(alignedBenchmark, command.getCustomPsd(),
            command.getCustomPed(), returnFactorScale));
    collector.throwIfAny();

    BenchmarkPeriodCalculationInput result = new BenchmarkPeriodCalculationInput();
    result.setWeightedAverageBenchmarkReturns(benchmarkResult.weightedAverage());
    result.setWeightedAveragePortfolioReturns(portfolioResult.weightedAverage());
    result.setCipsd(command.getCustomIntervalPsd());
    return result;
  }
}
