package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.PceExceptionCollector;

import java.time.LocalDate;
import java.util.Set;

public abstract class PeriodBenchmarkAbstractService<E extends PeriodResult, R extends PeriodCommand>
    extends
      PeriodAbstractService<E, R> {

  protected PeriodBenchmarkAbstractService(MonthlyReturnsService monthlyReturnsService, Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public BenchmarkPeriodCalculationInput buildPeriodCalculationInput(R command, ReturnFactorScale returnFactorScale) {
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
        () -> monthlyReturnsService.calculateWeightedAverageWithCped(alignedPortfolio, command.getCustomPed(),
            returnFactorScale));
    WeightedAverageResult<HoldingMonthlyReturns> benchmarkResult = collector.tryCatch(
        () -> monthlyReturnsService.calculateWeightedAverageWithCped(alignedBenchmark, command.getCustomPed(),
            returnFactorScale));
    collector.throwIfAny();

    BenchmarkPeriodCalculationInput result = new BenchmarkPeriodCalculationInput();
    result.setWeightedAverageBenchmarkReturns(benchmarkResult.weightedAverage());
    result.setWeightedAveragePortfolioReturns(portfolioResult.weightedAverage());
    result.setCipsd(command.getCustomIntervalPsd());
    return result;
  }
}
