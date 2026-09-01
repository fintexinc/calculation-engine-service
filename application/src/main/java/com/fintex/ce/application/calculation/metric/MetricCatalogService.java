package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.MetricInfo;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service for building and retrieving the metric catalog. Dynamically generates catalog entries from the
 * {@link CalculationMetric} enum, extracting metric ID, human-readable name, and category for each. The catalog is
 * never hand-maintained; it is always derived from the enum values and their metadata.
 */
@Service
public class MetricCatalogService {

  /**
   * Returns a list of all available metrics in the catalog, sorted by ID for consistency.
   *
   * @return List of metric information entries (id, name, category) for every supported metric
   */
  public List<MetricInfo> getCatalog() {
    return Arrays.stream(CalculationMetric.values())
        .map(metric -> new MetricInfo(
            metric.getValue(),
            metric.getUserFriendlyName(),
            metric.getCategory()))
        .toList();
  }
}
