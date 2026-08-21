package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a single supported calculation metric, containing the metric identifier and optional description.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Information about a supported portfolio calculation metric")
public class MetricInfo {

  @Schema(description = "Metric identifier used as the path parameter in POST /{metricName} requests",
      example = "trailing-total-returns")
  private String metricId;

  @Schema(description = "Short description of what this metric calculates",
      example = "Trailing total return over specified periods")
  private String description;
}
