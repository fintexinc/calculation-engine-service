package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;

/**
 * Information about a supported calculation metric, including its unique identifier and description.
 */
@Schema(description = "Information about a supported portfolio calculation metric")
@Builder
public record MetricInfo(
    @Schema(description = "The metric identifier (kebab-case), accepted by POST /{metricName}") String identifier,

    @Schema(description = "A short description of what the metric calculates") String description) {
}
