package com.fintex.ce.application.returns.pipeline;

import com.fintex.ce.application.returns.FxContext;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ProcessorsRunner;
import com.fintex.ce.application.returns.ReturnsRole;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.processor.ReturnsProcessor;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * End-to-end checks against the per-case pipeline strategies: each one must run applicable processors via the runner,
 * react to fatal errors, and (for weighted-average cases) emit a {@link WeightedAverageResult} built from the post-
 * processing snapshot.
 */
class MonthlyReturnsPipelineTest {

  private final WeightedAverageComponent weightedAverageComponent = mock(WeightedAverageComponent.class);

  @Test
  void shouldRunApplicableProcessors_whenPortfolioValidateCutAndFx() {
    RecordingProcessor passthrough = new RecordingProcessor(snapshot -> snapshot, true);
    PortfolioValidateCutAndFxPipeline pipeline = new PortfolioValidateCutAndFxPipeline(
        new ProcessorsRunner(List.of(passthrough)));

    ReturnsSnapshot<HoldingMonthlyReturns> result = pipeline.run(emptyPortfolioContext(), new CpedParams(null));

    assertThat(result).isNotNull();
    assertThat(passthrough.invocations).isEqualTo(1);
  }

  @Test
  void shouldThrow_whenPortfolioValidateCutAndFxPipelineLeavesFatalError() {
    RecordingProcessor injectFatal = new RecordingProcessor(
        snapshot -> snapshot.withAddedErrors(List.of(ErrorCode.CPED_BEFORE_PORTFOLIO_PSD.toException())),
        true);
    PortfolioValidateCutAndFxPipeline pipeline = new PortfolioValidateCutAndFxPipeline(
        new ProcessorsRunner(List.of(injectFatal)));

    assertThatThrownBy(() -> pipeline.run(emptyPortfolioContext(), new CpedParams(null)))
        .isInstanceOf(CalculationsFailedException.class);
  }

  @Test
  void shouldEmitWeightedAverage_whenPortfolioWeightedAverageWithCpsdAndCped() {
    RecordingProcessor passthrough = new RecordingProcessor(snapshot -> snapshot, true);
    NavigableMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.parse("2020-01-31"), BigDecimal.ONE);
    when(weightedAverageComponent.calculateWeightedAverage(any(), any())).thenReturn(expected);
    PortfolioWeightedAverageWithCpsdAndCpedPipeline pipeline = new PortfolioWeightedAverageWithCpsdAndCpedPipeline(
        new ProcessorsRunner(List.of(passthrough)), weightedAverageComponent);

    WeightedAverageResult<HoldingMonthlyReturns> result = pipeline.run(emptyPortfolioContext(),
        new CpsdCpedScaleParams(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31"),
            ReturnFactorScale.SCALE_OF_TWO));

    assertThat(result.weightedAverage()).isEqualTo(expected);
    assertThat(result.snapshot()).isNotNull();
    verify(weightedAverageComponent, times(1)).calculateWeightedAverage(any(), any());
  }

  private static MonthlyReturnsContext<HoldingMonthlyReturns> emptyPortfolioContext() {
    return new MonthlyReturnsContext<>(ReturnsSnapshot.empty(), FxContext.empty(), ReturnsRole.PORTFOLIO);
  }

  private static final class RecordingProcessor implements ReturnsProcessor {
    private final UnaryOperator<ReturnsSnapshot<?>> transform;
    private final boolean applicable;
    private int invocations;

    RecordingProcessor(UnaryOperator<ReturnsSnapshot<?>> transform, boolean applicable) {
      this.transform = transform;
      this.applicable = applicable;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends ReturnsData> ReturnsSnapshot<T> process(ReturnsSnapshot<T> snapshot, ProcessingContext context) {
      invocations++;
      return (ReturnsSnapshot<T>) transform.apply((ReturnsSnapshot) snapshot);
    }

    @Override
    public boolean isApplicable(ProcessingCase processingCase) {
      return applicable;
    }
  }
}
