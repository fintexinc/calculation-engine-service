package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.port.observability.CalculationObservability;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.calculation.CalculationOrchestrator;
import com.fintex.ce.calculation.CalculationService;

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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.adapter.rest.controller.PortfolioCalculationController.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
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

    EnumMap<CalculationMetric, CalculationService<?, ?, ?>> mockServices =
        new EnumMap<>(CalculationMetric.class);

    List<CalculationService<?, ?, ?>> serviceList = Arrays.stream(CalculationMetric.values())
        .<CalculationService<?, ?, ?>>map(metric -> {
          CalculationService<?, ?, ?> mock = mock(CalculationService.class);
          lenient().when(mock.getMetric()).thenReturn(metric);
          lenient().when(mock.requiredAttributes()).thenReturn(List.of());
          mockServices.put(metric, mock);
          return mock;
        })
        .toList();

    var controller = createController(serviceList);

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

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

    // Verify each entry has non-null metricId matching enum constant value
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

  private PortfolioCalculationController createController(List<CalculationService<?, ?, ?>> services) {
    SecurityAttributesFetcher fetcher = mock(SecurityAttributesFetcher.class);
    lenient().when(fetcher.fetch(any(), anyCollection(), any())).thenReturn(SecurityData.EMPTY);
    lenient().when(fetcher.fetch(any(), any(CompositeSecurityAttribute.class), any())).thenReturn(Map.of());

    CalculationOrchestrator orchestrator = mock(CalculationOrchestrator.class);
    RequestValidationFacade validationFacade = new RequestValidationFacade(List.of());
    CalculationObservability observability = mock(CalculationObservability.class);
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    return new PortfolioCalculationController(orchestrator, validationFacade, observability, validator.getValidator());
  }
}
