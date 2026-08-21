package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioCalculationController.class)
@AutoConfigureJson
class PortfolioCalculationControllerMetricsTest {

  private static final String BASE_PATH = "/api/v1/portfolio/calculations";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldReturnAllMetricsWithCorrectStructure() throws Exception {
    MvcResult mvcResult = mockMvc.perform(
        get(BASE_PATH + "/metrics")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    MetricInfo[] metricsArray = objectMapper.readValue(responseBody, MetricInfo[].class);
    List<MetricInfo> metrics = Arrays.asList(metricsArray);

    // Verify array length equals CalculationMetric.values().length
    assertThat(metrics).hasSize(CalculationMetric.values().length);

    // Verify each entry has non-null metricId matching enum constant name
    List<String> enumValues = Arrays.stream(CalculationMetric.values())
        .map(CalculationMetric::getValue)
        .toList();

    for (MetricInfo metric : metrics) {
      assertThat(metric.getMetricId()).isNotNull();
      assertThat(enumValues).contains(metric.getMetricId());
    }

    // Verify description field is present (may be null or empty)
    for (MetricInfo metric : metrics) {
      assertThat(metric).hasFieldOrProperty("description");
    }
  }
}
