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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
