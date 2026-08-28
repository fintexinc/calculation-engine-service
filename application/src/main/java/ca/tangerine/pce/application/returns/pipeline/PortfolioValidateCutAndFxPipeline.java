package ca.tangerine.pce.application.returns.pipeline;

import ca.tangerine.pce.application.returns.FxContext;
import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ProcessorsRunner;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;

import org.springframework.stereotype.Component;

/**
 * Runs validate + CPED cut + FX conversion without the per-holding PSD trim. Used by correlation calculations that need
 * the post-FX returns map before any start-date trim is applied.
 */
@Component
public class PortfolioValidateCutAndFxPipeline
    extends
      MonthlyReturnsPipeline<CpedParams, ReturnsSnapshot<HoldingMonthlyReturns>> {

  public PortfolioValidateCutAndFxPipeline(ProcessorsRunner runner) {
    super(runner);
  }

  @Override
  protected ProcessingCase processingCase() {
    return ProcessingCase.PORTFOLIO_PRE_PSD_TRIM;
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
