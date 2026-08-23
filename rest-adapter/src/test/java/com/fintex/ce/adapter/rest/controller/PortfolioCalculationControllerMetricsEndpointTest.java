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
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(PortfolioCalculationController.class)
class PortfolioCalculationControllerMetricsEndpointTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {}

  @Test
  void testListMetricsReturnsHttp200() throws Exception {
    mockMvc
        .perform(get("/api/v1/portfolio/calculations/metrics").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void testListMetricsReturnsJsonArray() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/portfolio/calculations/metrics")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    Object parsed = objectMapper.readValue(responseBody, Object.class);
    assertThat(parsed).isInstanceOf(List.class);
  }

  @Test
  void testListMetricsContainsAllEnumValues() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/portfolio/calculations/metrics")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> metrics =
        objectMapper.readValue(responseBody, List.class);

    Set<String> returnedIdentifiers = new HashSet<>();
    for (Map<String, Object> metric : metrics) {
      assertThat(metric).containsKey("identifier");
      returnedIdentifiers.add((String) metric.get("identifier"));
    }

    Set<String> expectedIdentifiers = new HashSet<>();
    for (CalculationMetric enumValue : CalculationMetric.values()) {
      expectedIdentifiers.add(enumValue.getValue());
    }

    assertThat(returnedIdentifiers).containsAll(expectedIdentifiers);
  }

  @Test
  void testListMetricsEachEntryHasMetricIdField() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/portfolio/calculations/metrics")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> metrics =
        objectMapper.readValue(responseBody, List.class);

    assertThat(metrics).isNotEmpty();
    for (Map<String, Object> metric : metrics) {
      assertThat(metric).containsKey("identifier");
      assertThat(metric.get("identifier")).isNotNull();
      assertThat(metric.get("identifier")).isInstanceOf(String.class);
    }
  }
}
