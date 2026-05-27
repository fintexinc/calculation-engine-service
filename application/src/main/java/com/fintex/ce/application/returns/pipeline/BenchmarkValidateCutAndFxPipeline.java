package com.fintex.ce.application.returns.pipeline;

import com.fintex.ce.application.returns.FxContext;
import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ProcessorsRunner;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;

import org.springframework.stereotype.Component;

/** Benchmark equivalent of {@link PortfolioValidateCutAndFxPipeline}. */
@Component
public class BenchmarkValidateCutAndFxPipeline
    extends
      MonthlyReturnsPipeline<CpedParams, ReturnsSnapshot<HoldingMonthlyReturns>> {

  public BenchmarkValidateCutAndFxPipeline(ProcessorsRunner runner) {
    super(runner);
  }

  @Override
  protected ProcessingCase processingCase() {
    return ProcessingCase.BENCHMARK_PRE_PSD_TRIM;
  }

  @Override
  protected ProcessingContext processingContext(CpedParams params, FxContext fxContext) {
    return ProcessingContext.of(null, params.cped(), fxContext);
  }

  @Override
  protected ReturnsSnapshot<HoldingMonthlyReturns> postProcess(ReturnsSnapshot<HoldingMonthlyReturns> processed,
      CpedParams params) {
    return processed;
  }
}
