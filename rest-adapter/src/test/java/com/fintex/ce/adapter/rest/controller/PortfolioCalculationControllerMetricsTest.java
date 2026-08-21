package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.application.calculation.orchestration.MetricCalculationOrchestrator;
import com.fintex.ce.application.config.DefaultDataProperties;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerMetricsTest {

  private static final String BASE_PATH = "/api/v1/portfolio/calculations";

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private MetricCalculationOrchestrator calculationOrchestrator;

  @Mock
  private RequestValidationFacade requestValidationFacade;

  @Mock
  private SecurityAttributesFetcher securityAttributesFetcher;

  @Mock
  private CalculationObservability calculationObservability;

  @Mock
  private DefaultDataProperties defaultDataProperties;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    PortfolioCalculationController controller = new PortfolioCalculationController(
        calculationOrchestrator,
        requestValidationFacade,
        securityAttributesFetcher,
        calculationObservability,
        defaultDataProperties);

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @Test
  void shouldReturnAllMetricsWithDescriptions_whenMetricsEndpointIsCalled() throws Exception {
    MvcResult result = mockMvc.perform(
        get(BASE_PATH + "/metrics")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    List<MetricInfo> metrics = objectMapper.readValue(responseBody, new TypeReference<List<MetricInfo>>() {});

    assertThat(metrics)
        .hasSize(CalculationMetric.values().length)
        .extracting(MetricInfo::getId)
        .containsExactlyInAnyOrder(
            Arrays.stream(CalculationMetric.values())
                .map(CalculationMetric::getValue)
                .toArray(String[]::new));

    assertThat(metrics)
        .allSatisfy(info -> assertThat(info.getDescription())
            .as("Metric %s should have a description", info.getId())
            .isNotBlank());
  }
}
