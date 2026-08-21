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
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.composite.CompositeCalculationResult;
import com.fintex.ce.model.domain.result.risk.StandardDeviationResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.model.dto.command.IncomeForecastCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.port.observability.CalculationDurationRecorder;
import com.fintex.ce.port.observability.CalculationObservability;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.adapter.rest.controller.PortfolioCalculationController.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private CalculationOrchestrator calculationOrchestrator;

  @Mock
  private MetricCalculationOrchestrator metricCalculationOrchestrator;

  @Mock
  private RequestValidationFacade validationFacade;

  @Mock
  private SecurityAttributesFetcher securityAttributesFetcher;

  @Mock
  private DefaultDataProperties defaultDataProperties;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    PortfolioCalculationController controller = new PortfolioCalculationController(
        calculationOrchestrator,
        metricCalculationOrchestrator,
        validationFacade,
        securityAttributesFetcher,
        defaultDataProperties);

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
