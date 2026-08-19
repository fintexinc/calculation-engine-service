package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.application.calculation.orchestration.MetricCalculationOrchestrator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.port.observability.CalculationObservability;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerMetricsEndpointTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock private MetricCalculationOrchestrator calculationOrchestrator;
  @Mock private RequestValidationFacade requestValidationFacade;
  @Mock private SecurityAttributesFetcher securityAttributesFetcher;
  @Mock private CalculationObservability calculationObservability;

  @BeforeEach
  void setUp() {
    PortfolioCalculationController controller =
        new PortfolioCalculationController(
            calculationOrchestrator,
            requestValidationFacade,
            securityAttributesFetcher,
            calculationObservability);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void testListMetricsEndpointReturns200WithAllMetrics() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/metrics").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    List<CalculationMetricInfo> metrics =
        Arrays.asList(objectMapper.readValue(responseBody, CalculationMetricInfo[].class));

    // Assert response is a JSON array
    assertNotNull(metrics);
    assertTrue(metrics.size() > 0);

    // Assert array contains exactly CalculationMetric.values().length entries
    assertEquals(
        CalculationMetric.values().length,
        metrics.size(),
        "Response should contain all CalculationMetric enum values");

    // Assert each entry has a non-null metric identifier
    Set<String> metricIdentifiers = new HashSet<>();
    for (CalculationMetricInfo metric : metrics) {
      assertNotNull(metric.metric(), "Metric identifier should not be null");
      assertNotNull(metric.description(), "Metric description should not be null");
      metricIdentifiers.add(metric.metric());
    }

    // Assert all CalculationMetric enum values are represented in the response
    Set<String> expectedMetrics = new HashSet<>();
    for (CalculationMetric cm : CalculationMetric.values()) {
      expectedMetrics.add(cm.getValue());
    }
    assertEquals(
        expectedMetrics,
        metricIdentifiers,
        "Response should contain all CalculationMetric enum values");
  }
}
