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

/** Benchmark equivalent of {@link PortfolioWeightedAverageWithCpsdAndCpedPipeline}. */
@Component
public class BenchmarkWeightedAverageWithCpsdAndCpedPipeline
    extends
      MonthlyReturnsPipeline<CpsdCpedScaleParams, WeightedAverageResult<HoldingMonthlyReturns>> {

  private final WeightedAverageComponent weightedAverageComponent;

  public BenchmarkWeightedAverageWithCpsdAndCpedPipeline(ProcessorsRunner runner,
      WeightedAverageComponent weightedAverageComponent) {
    super(runner);
    this.weightedAverageComponent = weightedAverageComponent;
  }

  @Override
  protected ProcessingCase processingCase() {
    return ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;
  }

  @Override
  protected ProcessingContext processingContext(CpsdCpedScaleParams params, FxContext fxContext) {
    return ProcessingContext.of(params.cpsd(), params.cped(), fxContext);
  }

  @Override
  protected WeightedAverageResult<HoldingMonthlyReturns> postProcess(ReturnsSnapshot<HoldingMonthlyReturns> processed,
      CpsdCpedScaleParams params) {
    NavigableMap<LocalDate, BigDecimal> weightedAverage = weightedAverageComponent.calculateWeightedAverage(
        processed.returnsMap(), params.scale());
    return new WeightedAverageResult<>(weightedAverage, processed);
  }
}
