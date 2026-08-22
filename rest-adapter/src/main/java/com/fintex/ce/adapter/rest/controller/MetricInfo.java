package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Information about a supported portfolio calculation metric, including its identifier and description.
 */
@Schema(description = "Information about a supported calculation metric")
public record MetricInfo(
    @Schema(description = "Metric identifier accepted by the portfolio calculations endpoint") String metricId,
    @Schema(description = "Description of the metric's purpose and calculation method") String description) {
}
