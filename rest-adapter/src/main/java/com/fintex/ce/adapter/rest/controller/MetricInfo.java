package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

/**
 * Information about a supported calculation metric, including its identifier and description.
 * Returned by the metrics endpoint to allow clients to discover all available metrics.
 */
@Value
@Schema(description = "Information about a supported calculation metric")
public class MetricInfo {

  @Schema(description = "The metric identifier used as the path parameter in the POST endpoint")
  String metric;

  @Schema(description = "Human-readable description of what this metric calculates")
  String description;
}
