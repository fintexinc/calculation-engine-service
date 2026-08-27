package ca.tangerine.pce.application.returns.pipeline;

import org.springframework.stereotype.Component;

import ca.tangerine.pce.application.returns.FxContext;
import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ProcessorsRunner;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;

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
