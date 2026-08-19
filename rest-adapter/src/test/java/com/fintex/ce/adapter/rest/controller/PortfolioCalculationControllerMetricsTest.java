package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(PortfolioCalculationController.class)
class PortfolioCalculationControllerMetricsTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void listMetrics_returnsAllCalculationMetrics() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/portfolio/calculations/metrics")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    List<MetricInfo> metrics = Arrays.asList(
        objectMapper.readValue(responseBody, MetricInfo[].class));

    // Verify we have exactly one entry per CalculationMetric enum value
    assertThat(metrics).hasSize(CalculationMetric.values().length);

    // Verify each metric has a non-null id
    assertThat(metrics)
        .allMatch(m -> m.getId() != null && !m.getId().isEmpty(),
            "All metrics must have a non-null and non-empty id");

    // Verify all enum values are represented
    Set<String> returnedIds = new HashSet<>();
    for (MetricInfo metric : metrics) {
      returnedIds.add(metric.getId());
    }

    Set<String> expectedIds = new HashSet<>();
    for (CalculationMetric enumValue : CalculationMetric.values()) {
      expectedIds.add(enumValue.getValue());
    }

    assertThat(returnedIds).isEqualTo(expectedIds);
  }
}
