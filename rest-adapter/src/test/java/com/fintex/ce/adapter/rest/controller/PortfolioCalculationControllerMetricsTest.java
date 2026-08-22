package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioCalculationController.class)
class PortfolioCalculationControllerMetricsTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
  }

  @Test
  void testListMetricsReturnsHttpOk() throws Exception {
    mockMvc
        .perform(get("/api/v1/portfolio/calculations/metrics").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void testListMetricsReturnsJsonArray() throws Exception {
    MvcResult result = mockMvc
        .perform(
            get("/api/v1/portfolio/calculations/metrics")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    assertTrue(
        responseBody.startsWith("["),
        "Response body should be a JSON array, got: " + responseBody);
  }

  @Test
  void testListMetricsReturnsCorrectNumberOfEntries() throws Exception {
    MvcResult result = mockMvc
        .perform(
            get("/api/v1/portfolio/calculations/metrics")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    List<?> metrics = objectMapper.readValue(responseBody, List.class);

    assertEquals(
        CalculationMetric.values().length,
        metrics.size(),
        "Response should contain exactly " + CalculationMetric.values().length + " metrics");
  }

  @Test
  void testListMetricsEachEntryHasNonEmptyMetricId() throws Exception {
    MvcResult result = mockMvc
        .perform(
            get("/api/v1/portfolio/calculations/metrics")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    List<MetricInfo> metrics = Arrays.asList(objectMapper.readValue(responseBody, MetricInfo[].class));

    for (MetricInfo metric : metrics) {
      assertNotNull(metric.metricId(), "metricId should not be null");
      assertFalse(metric.metricId().isEmpty(), "metricId should not be empty");
    }
  }

  @Test
  void testListMetricsContainsAllCalculationMetricValues() throws Exception {
    MvcResult result = mockMvc
        .perform(
            get("/api/v1/portfolio/calculations/metrics")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    List<MetricInfo> metrics = Arrays.asList(objectMapper.readValue(responseBody, MetricInfo[].class));

    Set<String> responseMetricIds = metrics.stream().map(MetricInfo::metricId).collect(HashSet::new, Set::add,
        Set::addAll);

    Set<String> expectedMetricIds = Arrays.stream(CalculationMetric.values())
        .map(CalculationMetric::getValue)
        .collect(HashSet::new, Set::add, Set::addAll);

    assertEquals(
        expectedMetricIds,
        responseMetricIds,
        "Response should contain all CalculationMetric enum values by identifier");
  }
}
