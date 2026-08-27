package ca.tangerine.pce.application.returns.pipeline;

import ca.tangerine.pce.application.returns.FxContext;
import ca.tangerine.pce.application.returns.MonthlyReturnsContext;
import ca.tangerine.pce.application.returns.PipelineResult;
import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ProcessorsRunner;
import ca.tangerine.pce.application.returns.ReturnsErrorPolicy;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.application.returns.WeightedAverageResult;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;

/**
 * Strategy base for running a specific monthly-returns processing pipeline. Each subclass owns one
 * {@link ProcessingCase} plus any post-pipeline computation (e.g. weighted-average collapse) and exposes it through the
 * inherited {@link #run} entry point.
 *
 * <p>
 * Implementations are split along two axes:
 * </p>
 * <ul>
 * <li><b>Role</b> — portfolio vs benchmark; each maps to a distinct {@code ProcessingCase} so the corresponding
 * processor list is composed correctly at startup.</li>
 * <li><b>Operation</b> — weighted-average with CPSD+CPED, weighted-average with CPED only, validate+cut+FX.</li>
 * </ul>
 *
 * <p>
 * Heterogeneous result type {@code R} so weighted-average subclasses can return {@link WeightedAverageResult} while
 * subclasses that don't collapse to a single series return {@link ReturnsSnapshot} directly — no {@code Void} shims, no
 * wrapper indirection.
 * </p>
 *
 * @param <P>
 *          per-invocation parameters (CPSD, CPED, scale, etc.).
 * @param <R>
 *          the pipeline's typed result.
 */
public abstract class MonthlyReturnsPipeline<P extends PipelineParams, R extends PipelineResult<HoldingMonthlyReturns>> {

  protected final ProcessorsRunner runner;

  protected MonthlyReturnsPipeline(ProcessorsRunner runner) {
    this.runner = runner;
  }

  public final R run(MonthlyReturnsContext<HoldingMonthlyReturns> context, P params) {
    ReturnsSnapshot<HoldingMonthlyReturns> processed = runner.run(context.snapshot(),
        processingContext(params, context.fxContext()),
        processingCase());
    ReturnsErrorPolicy.throwIfFatal(processed);
    return postProcess(processed, params);
  }

  protected abstract ProcessingCase processingCase();

  protected abstract ProcessingContext processingContext(P params, FxContext fxContext);

  protected abstract R postProcess(ReturnsSnapshot<HoldingMonthlyReturns> processed, P params);
}
