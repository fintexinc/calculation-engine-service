package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.adapter.rest.validation.validators.CipsdGreaterThanCpedReqValidator;
import com.fintex.ce.adapter.rest.validation.validators.HoldingReqValidator;
import com.fintex.ce.adapter.rest.validation.validators.HoldingsValidationProperties;
import com.fintex.ce.adapter.rest.validation.validators.HoldingsValidator;
import com.fintex.ce.adapter.rest.validation.validators.StandardDeviationPeriodsReqValidator;
import com.fintex.ce.adapter.rest.validation.validators.TrailingPeriodsReqValidator;
import com.fintex.ce.adapter.rest.validation.validators.TwelveMonthMinimumPeriodsReqValidator;
import com.fintex.ce.application.calculation.orchestration.MetricCalculationOrchestrator;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.calculation.CalculationOrchestrator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.port.observability.CalculationObservability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.adapter.rest.controller.PortfolioCalculationController.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerMetricsTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    var holdingsValidationProperties = new HoldingsValidationProperties();
    var holdingsValidator = new HoldingsValidator(holdingsValidationProperties);
    var requestValidators = Arrays.asList(
        new HoldingReqValidator(),
        new StandardDeviationPeriodsReqValidator(),
        new CipsdGreaterThanCpedReqValidator(),
        new TrailingPeriodsReqValidator(),
        new TwelveMonthMinimumPeriodsReqValidator(),
        holdingsValidator);
    var requestValidationFacade = new RequestValidationFacade(requestValidators);

    var orchestrator = new MetricCalculationOrchestrator();
    var defaultDataProperties = new DefaultDataProperties();
    var observability = new CalculationObservability() {};

    var controller = new PortfolioCalculationController(
        orchestrator,
        requestValidationFacade,
        defaultDataProperties,
        observability);

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @Test
  void shouldReturnAllMetrics_whenListingMetrics() throws Exception {
    MvcResult result = mockMvc.perform(
        get(BASE_PATH + "/metrics")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    MetricInfo[] metrics = objectMapper.readValue(responseBody, MetricInfo[].class);

    // Assert (1): response status is 200 — verified by .andExpect(status().isOk())
    // Assert (2): response body is a JSON array — verified by readValue to MetricInfo[]
    // Assert (3): array contains exactly CalculationMetric.values().length entries
    assertThat(metrics).hasLength(CalculationMetric.values().length);

    // Assert (4): each entry has a non-null metric identifier field
    assertThat(metrics)
        .allSatisfy(metric -> assertThat(metric.getMetricName()).isNotNull());

    // Assert (5): all CalculationMetric enum values are present in the response by identifier
    Set<String> metricNames = Arrays.stream(metrics)
        .map(MetricInfo::getMetricName)
        .collect(Collectors.toSet());
    Set<String> expectedMetrics = Arrays.stream(CalculationMetric.values())
        .map(CalculationMetric::getValue)
        .collect(Collectors.toSet());

    assertThat(metricNames).isEqualTo(expectedMetrics);
  }
}
