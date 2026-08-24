package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.calculation.CalculationOrchestrator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.port.observability.CalculationDurationRecorder;
import com.fintex.ce.port.observability.CalculationObservability;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerTest {

  private static final String BASE_PATH = "/api/v1/portfolio/calculations";

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private CalculationOrchestrator calculationOrchestrator;

  @Mock
  private RequestValidationFacade requestValidationFacade;

  @Mock
  private CalculationObservability calculationObservability;

  @Mock
  private CalculationDurationRecorder durationRecorder;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    PortfolioCalculationController controller = new PortfolioCalculationController(
        calculationOrchestrator,
        requestValidationFacade,
        calculationObservability);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @Test
  void shouldReturnAllMetrics_whenListMetricsRequested() throws Exception {
    MvcResult mvcResult = mockMvc.perform(
        get(BASE_PATH + "/metrics")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    MetricInfo[] metrics = objectMapper.readValue(responseBody, MetricInfo[].class);

    // Verify we have exactly as many metrics as enum values
    assertThat(metrics)
        .hasSize(CalculationMetric.values().length);

    // Verify all enum values are represented
    Set<String> metricIds = new HashSet<>();
    for (MetricInfo metric : metrics) {
      assertThat(metric.getId()).isNotNull();
      metricIds.add(metric.getId());
    }

    Set<String> expectedIds = new HashSet<>();
    for (CalculationMetric enumValue : CalculationMetric.values()) {
      expectedIds.add(enumValue.getValue());
    }

    assertThat(metricIds).isEqualTo(expectedIds);

    // Verify each metric has a non-empty description
    for (MetricInfo metric : metrics) {
      assertThat(metric.getDescription())
          .isNotNull()
          .isNotEmpty();
    }
  }
}
