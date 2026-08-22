package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.application.calculation.orchestration.MetricCalculationOrchestrator;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.calculation.CalculationOrchestrator;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.port.observability.CalculationDurationRecorder;
import com.fintex.ce.port.observability.CalculationObservability;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;
import com.fintex.wm.commons.domain.DataProvider;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Create mock services for all metrics
    List<CalculationService<?, ?, ?>> mockServices = Arrays.stream(CalculationMetric.values())
        .<CalculationService<?, ?, ?>>map(this::createMockService)
        .toList();

    // Create the controller with all necessary dependencies
    var controller = createController(mockServices);

    // Set up MockMvc with standalone setup
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  private CalculationService<?, ?, ?> createMockService(CalculationMetric metric) {
    CalculationService<?, ?, ?> mock = mock(CalculationService.class);
    lenient().when(mock.getMetric()).thenReturn(metric);
    lenient().when(mock.requiredAttributes()).thenReturn(List.of());
    return mock;
  }

  private PortfolioCalculationController createController(List<CalculationService<?, ?, ?>> services) {
    SecurityAttributesFetcher fetcher = mock(SecurityAttributesFetcher.class);
    lenient().when(fetcher.fetch(any(), anyCollection(), any())).thenReturn(
        com.fintex.ce.model.domain.security.SecurityData.EMPTY);
    lenient().when(fetcher.fetch(any(), any(com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute.class), any()))
        .thenReturn(Map.of());

    CalculationOrchestrator orchestrator = new MetricCalculationOrchestrator(
        services,
        fetcher,
        new DefaultDataProperties(List.of(DataProvider.MORNINGSTAR, DataProvider.FMP)),
        CalculationDurationRecorder.NO_OP);

    LocalValidatorFactoryBean beanValidator = new LocalValidatorFactoryBean();
    beanValidator.afterPropertiesSet();

    return new PortfolioCalculationController(
        orchestrator,
        new RequestValidationFacade(List.of()),
        mock(CalculationObservability.class),
        beanValidator);
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
