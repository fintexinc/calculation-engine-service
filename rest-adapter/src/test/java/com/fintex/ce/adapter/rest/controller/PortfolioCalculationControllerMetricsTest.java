package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.wm.commons.domain.currency.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.adapter.rest.controller.PortfolioCalculationController.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioCalculationController.class)
class PortfolioCalculationControllerMetricsTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldReturnAllMetrics_whenMetricsListRequested() throws Exception {
    MvcResult mvcResult = mockMvc.perform(
        get(BASE_PATH + "/metrics")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    MetricInfo[] metrics = objectMapper.readValue(responseBody, MetricInfo[].class);

    // Verify we have all metrics
    assertThat(metrics).hasSize(CalculationMetric.values().length);

    // Verify each metric has both identifier and description
    for (MetricInfo metric : metrics) {
      assertThat(metric.getMetric())
          .as("Metric identifier should not be null or empty for: %s", metric)
          .isNotNull()
          .isNotEmpty();
      assertThat(metric.getDescription())
          .as("Description should not be null for metric: %s", metric.getMetric())
          .isNotNull();
    }

    // Verify all CalculationMetric enum values are present
    Set<String> returnedMetrics = Arrays.stream(metrics)
        .map(MetricInfo::getMetric)
        .collect(Collectors.toSet());
    Set<String> expectedMetrics = Arrays.stream(CalculationMetric.values())
        .map(CalculationMetric::getValue)
        .collect(Collectors.toSet());

    assertThat(returnedMetrics)
        .as("Returned metrics should contain all CalculationMetric enum values")
        .isEqualTo(expectedMetrics);
  }
}
