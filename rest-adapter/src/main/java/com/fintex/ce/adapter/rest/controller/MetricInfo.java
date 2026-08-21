package com.fintex.ce.adapter.rest.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * Information about a supported portfolio calculation metric. Returned by the metrics endpoint to allow clients to
 * discover available metrics and their descriptions.
 */
@Getter
@Schema(description = "Information about a supported portfolio calculation metric")
public class MetricInfo {

  @Schema(description = "Metric identifier (the value accepted by POST /{metricName} endpoint)", example = "trailing-total-returns")
  private final String id;

  @Schema(description = "Short description of the metric", example = "Trailing total return over specified periods")
  private final String description;

  @JsonCreator
  public MetricInfo(@JsonProperty("id") String id, @JsonProperty("description") String description) {
    this.id = id;
    this.description = description;
  }
}
