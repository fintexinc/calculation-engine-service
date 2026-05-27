package com.fintex.ce.application.returns.pipeline;

import com.fintex.ce.application.util.ReturnFactorScale;

import java.time.LocalDate;

/** Pipeline parameters for weighted-average pipelines that take only a custom performance end date. */
public record CpedScaleParams(LocalDate cped, ReturnFactorScale scale) implements PipelineParams {
}