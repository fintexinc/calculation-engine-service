package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Information about a supported portfolio calculation metric, suitable for listing available calculations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Metadata about a supported portfolio calculation metric")
public class MetricInfo {

  @Schema(description = "The metric identifier (kebab-case), to be used in the POST /{metricName} endpoint", example = "trailing-total-returns")
  private String metricName;

  @Schema(description = "Human-readable description of this metric", example = "Trailing total return over specified periods")
  private String description;
}
