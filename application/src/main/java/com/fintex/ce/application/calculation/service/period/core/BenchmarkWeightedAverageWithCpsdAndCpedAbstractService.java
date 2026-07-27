package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.returns.pipeline.CpsdCpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.contract.CustomPsdProvider;
import com.fintex.ce.model.error.PceExceptionCollector;

import java.time.LocalDate;

/**
 * Benchmark-side counterpart of {@link WeightedAverageWithCpsdAndCpedAbstractService}: fetches both portfolio and
 * benchmark contexts, aligns them to a common performance window, and runs the CPSD+CPED weighted-average pipeline for
 * both roles.
 */
public abstract class BenchmarkWeightedAverageWithCpsdAndCpedAbstractService<C extends PeriodCommand & CustomPsdProvider, R extends PeriodResult>
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<C, R> {

  protected final BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider;
  protected final BenchmarkWeightedAverageWithCpsdAndCpedPipeline benchmarkWeightedAverageWithCpsdAndCped;

  protected BenchmarkWeightedAverageWithCpsdAndCpedAbstractService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      BenchmarkWeightedAverageWithCpsdAndCpedPipeline benchmarkWeightedAverageWithCpsdAndCped) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.benchmarkMonthlyReturnsContextProvider = benchmarkMonthlyReturnsContextProvider;
    this.benchmarkWeightedAverageWithCpsdAndCped = benchmarkWeightedAverageWithCpsdAndCped;
  }

  /**
   * Builds portfolio + benchmark contexts from the supplied returns, aligns them to the common performance window
   * (common start and end date), and runs the CPSD+CPED pipeline on each. Errors from any of the four steps (two
   * context builds + two pipeline runs) are collected so the caller sees the full picture rather than just the first
   * failure.
   */
  protected BenchmarkPeriodCalculationInput buildBenchmarkInput(C command, ReturnFactorScale scale,
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

    CpsdCpedScaleParams params = new CpsdCpedScaleParams(command.getCustomPsd(), command.getCustomPed(), scale);
    WeightedAverageResult<HoldingMonthlyReturns> portfolioResult = collector.tryCatch(
        () -> portfolioWeightedAverageWithCpsdAndCped.run(alignedPortfolio, params));
    WeightedAverageResult<HoldingMonthlyReturns> benchmarkResult = collector.tryCatch(
        () -> benchmarkWeightedAverageWithCpsdAndCped.run(alignedBenchmark, params));
    collector.throwIfAny();

    BenchmarkPeriodCalculationInput result = new BenchmarkPeriodCalculationInput();
    result.setWeightedAverageBenchmarkReturns(benchmarkResult.weightedAverage());
    result.setWeightedAveragePortfolioReturns(portfolioResult.weightedAverage());
    result.setCipsd(command.getCustomIntervalPsd());
    return result;
  }
}
