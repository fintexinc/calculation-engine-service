package ca.tangerine.pce.application.returns.pipeline;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import ca.tangerine.pce.application.returns.FxContext;
import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ProcessorsRunner;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.application.returns.WeightedAverageComponent;
import ca.tangerine.pce.application.returns.WeightedAverageResult;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;

/** Benchmark equivalent of {@link PortfolioWeightedAverageWithCpedPipeline}. */
@Component
public class BenchmarkWeightedAverageWithCpedPipeline
    extends
      MonthlyReturnsPipeline<CpedScaleParams, WeightedAverageResult<HoldingMonthlyReturns>> {

  private final WeightedAverageComponent weightedAverageComponent;

  public BenchmarkWeightedAverageWithCpedPipeline(ProcessorsRunner runner,
      WeightedAverageComponent weightedAverageComponent) {
    super(runner);
    this.weightedAverageComponent = weightedAverageComponent;
  }

  @Override
  protected ProcessingCase processingCase() {
    return ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPED_ONLY;
  }

  @Override
  protected ProcessingContext processingContext(CpedScaleParams params, FxContext fxContext) {
    return ProcessingContext.of(null, params.cped(), fxContext);
  }

  @Override
  protected WeightedAverageResult<HoldingMonthlyReturns> postProcess(ReturnsSnapshot<HoldingMonthlyReturns> processed,
      CpedScaleParams params) {
    NavigableMap<LocalDate, BigDecimal> weightedAverage = weightedAverageComponent.calculateWeightedAverage(
        processed.returnsMap(), params.scale());
    return new WeightedAverageResult<>(weightedAverage, processed);
  }
}
