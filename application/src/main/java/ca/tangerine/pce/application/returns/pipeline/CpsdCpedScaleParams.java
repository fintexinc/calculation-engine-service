package ca.tangerine.pce.application.returns.pipeline;

import java.time.LocalDate;

import ca.tangerine.pce.application.util.ReturnFactorScale;

/** Pipeline parameters for weighted-average pipelines that take both custom start and end dates. */
public record CpsdCpedScaleParams(LocalDate cpsd, LocalDate cped, ReturnFactorScale scale) implements PipelineParams {
}