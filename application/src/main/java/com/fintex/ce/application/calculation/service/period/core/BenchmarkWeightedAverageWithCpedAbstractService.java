package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.CpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.PceExceptionCollector;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.time.LocalDate;
import java.util.Set;

/**
 * Benchmark-side counterpart of {@link WeightedAverageWithCpedAbstractService}: fetches both portfolio and benchmark
 * contexts, aligns them to a common performance window, and runs the CPED-only weighted-average pipeline for both
 * roles.
 */
public abstract class BenchmarkWeightedAverageWithCpedAbstractService<C extends PeriodCommand, R extends PeriodResult>
    extends
      WeightedAverageWithCpedAbstractService<C, R> {

  protected final BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider;
  protected final BenchmarkWeightedAverageWithCpedPipeline benchmarkWeightedAverageWithCped;

  protected BenchmarkWeightedAverageWithCpedAbstractService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      BenchmarkWeightedAverageWithCpedPipeline benchmarkWeightedAverageWithCped,
      Set<TimePeriod> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
    this.benchmarkMonthlyReturnsContextProvider = benchmarkMonthlyReturnsContextProvider;
    this.benchmarkWeightedAverageWithCped = benchmarkWeightedAverageWithCped;
  }

  @Override
  public BenchmarkPeriodCalculationInput buildPeriodCalculationInput(C command, ReturnFactorScale returnFactorScale,
      PortfolioBenchmarkReturns returnsData) {
    PceExceptionCollector collector = new PceExceptionCollector();

    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = collector.tryCatch(
        () -> portfolioMonthlyReturnsContextProvider.get(command.getHoldings(), command.getCurrency(),
            returnsData.portfolio()));
    MonthlyReturnsContext<HoldingMonthlyReturns> benchmarkContext = collector.tryCatch(
        () -> benchmarkMonthlyReturnsContextProvider.get(command.getBenchmarkHoldings(), command.getCurrency(),
            returnsData.benchmark()));
    collector.throwIfAny();

    LocalDate commonStart = portfolioContext.commonPerformanceStartDate(benchmarkContext);
    LocalDate commonEnd = portfolioContext.commonPerformanceEndDate(benchmarkContext);
    MonthlyReturnsContext<HoldingMonthlyReturns> alignedPortfolio = portfolioContext.trimToRange(commonStart,
        commonEnd);
    MonthlyReturnsContext<HoldingMonthlyReturns> alignedBenchmark = benchmarkContext.trimToRange(commonStart,
        commonEnd);

    CpedScaleParams portfolioParams = new CpedScaleParams(command.getCustomPed(), returnFactorScale);
    LocalDate benchmarkCped = portfolioContext.isCustomPedAfterPerformanceEndDate(command.getCustomPed())
        ? commonEnd
        : command.getCustomPed();
    CpedScaleParams benchmarkParams = new CpedScaleParams(benchmarkCped, returnFactorScale);
    WeightedAverageResult<HoldingMonthlyReturns> portfolioResult = collector.tryCatch(
        () -> portfolioWeightedAverageWithCped.run(alignedPortfolio, portfolioParams));
    WeightedAverageResult<HoldingMonthlyReturns> benchmarkResult = collector.tryCatch(
        () -> benchmarkWeightedAverageWithCped.run(alignedBenchmark, benchmarkParams));
    collector.throwIfAny();

    BenchmarkPeriodCalculationInput result = new BenchmarkPeriodCalculationInput();
    result.setWeightedAverageBenchmarkReturns(benchmarkResult.weightedAverage());
    result.setWeightedAveragePortfolioReturns(portfolioResult.weightedAverage());
    result.setCipsd(command.getCustomIntervalPsd());
    return result;
  }
}
