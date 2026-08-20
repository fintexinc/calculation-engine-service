package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

/**
 * Metadata for a supported portfolio calculation metric. Used by the metrics listing endpoint to describe each
 * available calculation.
 */
@Data
@Builder
@Schema(description = "Metadata for a supported portfolio calculation metric")
public class MetricInfo {

  @Schema(description = "The metric identifier, as accepted by POST /{metricName}", example = "trailing-total-returns")
  private String metric;

  @Schema(description = "Short description of what the metric calculates", example = "Trailing total return over specified periods")
  private String description;

}
