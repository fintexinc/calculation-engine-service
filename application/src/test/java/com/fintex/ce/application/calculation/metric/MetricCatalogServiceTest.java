package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.MetricInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MetricCatalogServiceTest {

  private MetricCatalogService metricCatalogService;

  @BeforeEach
  void setUp() {
    metricCatalogService = new MetricCatalogService();
  }

  @Test
  void getCatalog_returnsAllMetrics() {
    List<MetricInfo> catalog = metricCatalogService.getCatalog();
    assertEquals(CalculationMetric.values().length, catalog.size(),
        "Catalog should contain all CalculationMetric enum values");
  }

  @Test
  void getCatalog_noDuplicateIds() {
    List<MetricInfo> catalog = metricCatalogService.getCatalog();
    Set<String> uniqueIds = catalog.stream().map(MetricInfo::id).collect(Collectors.toSet());
    assertEquals(catalog.size(), uniqueIds.size(),
        "Catalog should have no duplicate metric IDs");
  }

  @Test
  void getCatalog_allMetricsHaveValidMetadata() {
    List<MetricInfo> catalog = metricCatalogService.getCatalog();
    for (MetricInfo metric : catalog) {
      assertNotNull(metric.id(), "Metric ID should not be null");
      assertFalse(metric.id().isEmpty(), "Metric ID should not be empty");
      assertNotNull(metric.name(), "Metric name should not be null");
      assertFalse(metric.name().isEmpty(), "Metric name should not be empty");
      assertNotNull(metric.category(), "Metric category should not be null");
      assertFalse(metric.category().isEmpty(), "Metric category should not be empty");
    }
  }

  @Test
  void getCatalog_metricsHaveValidCategories() {
    List<MetricInfo> catalog = metricCatalogService.getCatalog();
    Set<String> validCategories = Set.of("Returns", "Risk", "Allocations", "Fees", "Income", "Analysis");
    for (MetricInfo metric : catalog) {
      assertTrue(validCategories.contains(metric.category()),
          "Metric " + metric.id() + " has invalid category: " + metric.category());
    }
  }

  @Test
  void getCatalog_sharpeRatioMetricPresent() {
    List<MetricInfo> catalog = metricCatalogService.getCatalog();
    MetricInfo sharpeRatio = catalog.stream()
        .filter(m -> m.id().equals(CalculationMetric.SHARPE_RATIO.getValue()))
        .findFirst()
        .orElse(null);
    assertNotNull(sharpeRatio, "Sharpe Ratio metric should be in catalog");
    assertEquals("Risk", sharpeRatio.category(), "Sharpe Ratio should be in Risk category");
  }

  @Test
  void getCatalog_trailingTotalReturnsMetricPresent() {
    List<MetricInfo> catalog = metricCatalogService.getCatalog();
    MetricInfo trailing = catalog.stream()
        .filter(m -> m.id().equals(CalculationMetric.TRAILING_TOTAL_RETURNS.getValue()))
        .findFirst()
        .orElse(null);
    assertNotNull(trailing, "Trailing Total Returns metric should be in catalog");
    assertEquals("Returns", trailing.category(), "Trailing Total Returns should be in Returns category");
  }
}
