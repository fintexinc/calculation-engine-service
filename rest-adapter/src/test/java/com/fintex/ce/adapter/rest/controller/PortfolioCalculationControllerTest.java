package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.calculation.CalculationOrchestrator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.validation.Validator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.adapter.rest.controller.PortfolioCalculationController.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private CalculationOrchestrator calculationOrchestrator;

  @Mock
  private RequestValidationFacade validationFacade;

  @Mock
  private CalculationObservability calculationObservability;

  @Mock
  private Validator validator;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    PortfolioCalculationController controller = new PortfolioCalculationController(
        calculationOrchestrator,
        validationFacade,
        calculationObservability,
        validator);

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @Test
  void shouldReturnAllMetrics_whenMetricsEndpointRequested() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(BASE_PATH + "/metrics"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    MetricInfo[] metrics = objectMapper.readValue(responseBody, MetricInfo[].class);

    // Verify response is a JSON array
    assertThat(metrics).isNotNull();
    assertThat(metrics).isNotEmpty();

    // Verify response contains exactly one entry per CalculationMetric enum value
    Set<String> metricIdentifiers = Arrays.stream(metrics)
        .map(MetricInfo::getIdentifier)
        .collect(Collectors.toSet());

    Set<String> expectedIdentifiers = Arrays.stream(CalculationMetric.values())
        .map(CalculationMetric::getValue)
        .collect(Collectors.toSet());

    assertThat(metricIdentifiers)
        .as("response should contain exactly one entry per CalculationMetric enum value")
        .isEqualTo(expectedIdentifiers);

    assertThat(metrics.length)
        .as("array should contain exactly one entry per CalculationMetric enum value")
        .isEqualTo(CalculationMetric.values().length);

    // Verify each entry has a non-null metric identifier matching the enum name
    for (MetricInfo metric : metrics) {
      assertThat(metric.getIdentifier())
          .as("metric identifier should not be null")
          .isNotNull();

      boolean matchesEnum = Arrays.stream(CalculationMetric.values())
          .anyMatch(e -> e.getValue().equals(metric.getIdentifier()));
      assertThat(matchesEnum)
          .as("metric identifier '%s' should match a CalculationMetric enum value", metric.getIdentifier())
          .isTrue();
    }

    // Verify description field is present (may be null for metrics without descriptions)
    for (MetricInfo metric : metrics) {
      assertThat(metric.getDescription())
          .as("metric '%s' should have a description field", metric.getIdentifier())
          .isNotNull();
    }
  }
}
