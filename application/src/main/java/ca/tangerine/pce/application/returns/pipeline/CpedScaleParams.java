package ca.tangerine.pce.application.returns.pipeline;

import java.time.LocalDate;

import ca.tangerine.pce.application.util.ReturnFactorScale;

/** Pipeline parameters for weighted-average pipelines that take only a custom performance end date. */
public record CpedScaleParams(LocalDate cped, ReturnFactorScale scale) implements PipelineParams {
}