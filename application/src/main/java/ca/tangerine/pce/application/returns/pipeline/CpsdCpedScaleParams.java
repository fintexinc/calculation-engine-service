package ca.tangerine.pce.application.returns.pipeline;

import ca.tangerine.pce.application.util.ReturnFactorScale;

import java.time.LocalDate;

/** Pipeline parameters for weighted-average pipelines that take both custom start and end dates. */
public record CpsdCpedScaleParams(LocalDate cpsd, LocalDate cped, ReturnFactorScale scale) implements PipelineParams {
}