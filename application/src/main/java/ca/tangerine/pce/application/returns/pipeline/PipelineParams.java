package ca.tangerine.pce.application.returns.pipeline;

/**
 * Marker for the per-invocation parameter records accepted by {@link MonthlyReturnsPipeline} subclasses. Sealed so the
 * set is closed at compile time — each parameter record corresponds to one pipeline shape.
 */
public sealed interface PipelineParams permits CpedParams, CpedScaleParams, CpsdCpedScaleParams {
}
