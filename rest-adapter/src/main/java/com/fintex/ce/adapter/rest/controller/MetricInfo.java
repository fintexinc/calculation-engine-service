package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Information about a supported portfolio calculation metric. Returned by the metrics endpoint to allow clients to
 * discover available metrics and their descriptions.
 */
@Getter
@AllArgsConstructor
@Schema(description = "Information about a supported portfolio calculation metric")
public class MetricInfo {

  @Schema(description = "Metric identifier (the value accepted by POST /{metricName} endpoint)", example = "trailing-total-returns")
  private final String id;

  @Schema(description = "Short description of the metric", example = "Trailing total return over specified periods")
  private final String description;
}
