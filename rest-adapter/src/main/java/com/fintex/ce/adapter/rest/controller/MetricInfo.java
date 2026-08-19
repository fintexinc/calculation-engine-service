package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Information about a supported calculation metric.
 * Used to list available metrics through the metrics discovery endpoint.
 */
@Value
@Builder
@Schema(description = "Information about a supported calculation metric")
public class MetricInfo {

  @Schema(description = "Metric identifier accepted by calculation endpoints", example = "sharpe-ratio")
  String id;

  @Schema(description = "Short description of the metric", example = "Risk-adjusted return relative to risk-free rate")
  String description;
}
