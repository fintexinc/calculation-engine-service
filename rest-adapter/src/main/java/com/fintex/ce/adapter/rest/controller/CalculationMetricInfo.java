package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;

import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Information about a supported calculation metric, including its identifier and description.
 * The description is extracted from the metric's {@link Schema} annotation on its enum field.
 */
@Schema(description = "Information about a supported calculation metric")
public record CalculationMetricInfo(
    @Schema(description = "Metric identifier accepted by the POST /{metricName} endpoint")
    String metric,
    @Schema(description = "Short description of the metric (from enum definition)")
    String description) {

  /**
   * Returns a list of all supported calculation metrics with their descriptions.
   * The descriptions are extracted from the enum's @Schema annotations.
   */
  public static List<CalculationMetricInfo> allMetrics() {
    return Arrays.stream(CalculationMetric.values())
        .map(m -> new CalculationMetricInfo(m.getValue(), extractDescription(m)))
        .toList();
  }

  private static String extractDescription(CalculationMetric metric) {
    try {
      // Access the enum field to get its @Schema annotation
      Field field = CalculationMetric.class.getDeclaredField(metric.name());
      Schema schema = field.getAnnotation(Schema.class);
      if (schema != null && !schema.description().isEmpty()) {
        return schema.description();
      }
    } catch (NoSuchFieldException e) {
      // Field not found, fall back to user-friendly name
    }
    return metric.getUserFriendlyName();
  }
}
