package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Value;

/**
 * Metadata about a supported calculation metric. Used by the metrics catalog endpoint to list all available metrics
 * with their identifying names and descriptions.
 */
@Value
@Builder
@Schema(description = "Metadata for a supported portfolio calculation metric")
public class MetricInfo {

  @Schema(description = "The metric identifier (kebab-case) — passed as the metricName in POST requests or within composite commands", example = "trailing-total-returns")
  private final String id;

  @Schema(description = "Human-readable description of the metric", example = "Trailing total return over specified periods")
  private final String description;
}
