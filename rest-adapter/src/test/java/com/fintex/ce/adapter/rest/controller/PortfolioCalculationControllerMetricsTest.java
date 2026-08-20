package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioCalculationController.class)
class PortfolioCalculationControllerMetricsTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testListMetricsReturnsAllCalculationMetrics() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/portfolio/calculations/metrics"))
        .andExpect(status().isOk())
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    List<MetricInfo> metrics = Arrays.asList(
        objectMapper.readValue(responseBody, MetricInfo[].class)
    );

    // Assert response is a JSON array
    assertThat(metrics).isNotNull();

    // Assert exactly one entry per CalculationMetric enum value
    Set<String> metricNames = new HashSet<>();
    for (MetricInfo metric : metrics) {
      metricNames.add(metric.getMetric());
    }

    Set<String> expectedMetrics = new HashSet<>();
    for (CalculationMetric cm : CalculationMetric.values()) {
      expectedMetrics.add(cm.getValue());
    }

    assertThat(metrics).hasSize(CalculationMetric.values().length);
    assertThat(metricNames).isEqualTo(expectedMetrics);

    // Assert each entry has a non-null metric identifier
    for (MetricInfo metric : metrics) {
      assertThat(metric.getMetric()).isNotNull();
      assertThat(metric.getMetric()).isNotEmpty();
    }

    // Assert description field is present (may be null or empty)
    for (MetricInfo metric : metrics) {
      assertThat(metric).hasFieldOrProperty("description");
    }
  }
}
