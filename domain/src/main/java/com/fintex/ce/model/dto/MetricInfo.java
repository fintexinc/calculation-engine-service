package com.fintex.ce.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Metadata for a single metric in the catalog. Used to describe available metrics for client consumption.
 *
 * @param id
 *          The metric identifier (kebab-case name), e.g. "sharpe-ratio"
 * @param name
 *          Human-readable name of the metric, e.g. "Sharpe Ratio"
 * @param category
 *          The metric's category, e.g. "Risk", "Returns", "Allocation"
 */
@Schema(description = "Metric catalog entry with metadata")
public record MetricInfo(
    @Schema(description = "Metric identifier (kebab-case)", example = "sharpe-ratio") String id,
    @Schema(description = "Human-readable metric name", example = "Sharpe Ratio") String name,
    @Schema(description = "Metric category", example = "Risk") String category) {
}
