package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.ReturnsData;

/**
 * Marker for the typed result of a monthly-returns pipeline run. Sealed so the upper bound on a pipeline's {@code R}
 * type parameter pins it to the closed set of pipeline outputs — either the unmodified {@link ReturnsSnapshot} (for
 * validate/cut/FX pipelines) or a {@link WeightedAverageResult} (for weighted-average pipelines).
 */
public sealed interface PipelineResult<T extends ReturnsData> permits ReturnsSnapshot, WeightedAverageResult {
}
