package ca.tangerine.pce.application.returns.pipeline;

import java.time.LocalDate;

/** Pipeline parameters carrying only a custom performance end date. */
public record CpedParams(LocalDate cped) implements PipelineParams {
}