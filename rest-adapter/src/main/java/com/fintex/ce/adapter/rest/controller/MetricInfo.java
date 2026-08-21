package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Describes a supported portfolio calculation metric.
 *
 * <p>
 * Used to list available metrics in the metrics discovery endpoint. The identifier matches the
 * value accepted by the POST /{metricName} endpoint.
 */
@Value
@RequiredArgsConstructor
@Schema(description = "Description of a supported portfolio calculation metric")
public class MetricInfo {

  @Schema(description = "Metric identifier (accepted by POST /{metricName} endpoint)", example = "trailing-total-returns")
  String identifier;

  @Schema(description = "Short description of what the metric calculates")
  String description;
}
