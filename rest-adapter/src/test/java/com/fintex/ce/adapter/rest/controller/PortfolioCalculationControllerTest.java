package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.adapter.rest.validation.validators.CipsdGreaterThanCpedReqValidator;
import com.fintex.ce.adapter.rest.validation.validators.HoldingReqValidator;
import com.fintex.ce.adapter.rest.validation.validators.PeriodReqValidator;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.model.dto.command.IncomeForecastCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.fintex.ce.adapter.rest.controller.PortfolioCalculationController.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private EnumMap<CalculationMetric, CalculationService> mockServices;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    mockServices = new EnumMap<>(CalculationMetric.class);
    List<CalculationService<?, ?>> serviceList = Arrays.stream(CalculationMetric.values())
        .map(this::createMockService)
        .toList();

    var controller = new PortfolioCalculationController(
        serviceList,
        new com.fintex.ce.adapter.rest.validation.RequestValidationFacade(java.util.List.of()));

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("calculationMetricArguments")
  @SuppressWarnings("unchecked")
  void shouldReturnMappedResponse_whenValidMetricRequested(
      CalculationMetric metric,
      CalculationCommand command,
      Object serviceResult,
      Class<? extends BaseCalculationResult> responseType) throws Exception {
    lenient().when(mockServices.get(metric).perform(any())).thenReturn((BaseCalculationResult) serviceResult);

    command.setMetric(metric);
    String requestBody = objectMapper.writeValueAsString(command);

    MvcResult mvcResult = mockMvc.perform(
        post(BASE_PATH + "/" + metric.getValue())
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(result -> {
          if (result.getResolvedException() != null) {
            throw new AssertionError("Controller threw: " + result.getResolvedException().getMessage(),
                result.getResolvedException());
          }
        })
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    verify(mockServices.get(metric)).perform(any());

    String responseBody = mvcResult.getResponse().getContentAsString();
    BaseCalculationResult actual = objectMapper.readValue(responseBody, responseType);

    assertThat(actual)
        .isNotNull()
        .isInstanceOf(responseType)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(serviceResult);
  }

  @Test
  void shouldThrowException_whenUnknownMetricRequested() {
    String requestBody = """
        {"metric": "trailing-total-returns", "currency": "CAD", "holdings": [
          {"value": 1, "holdingType": "MUTUAL_FUND_CANADA",
           "securityIdentifier": {"id": "DUMMY", "idType": "TICKER"}}
        ]}
        """;

    assertThatThrownBy(() -> mockMvc.perform(
        post(BASE_PATH + "/unknown-metric")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)))
        .hasCauseInstanceOf(CalculationException.class)
        .hasMessageContaining("unknown-metric");
  }

  @Test
  void shouldThrowException_whenMetricInBodyMismatchesPathParameter() {
    String requestBody = """
        {"metric": "sharpe-ratio", "currency": "CAD", "holdings": [
          {"value": 1, "holdingType": "MUTUAL_FUND_CANADA",
           "securityIdentifier": {"id": "DUMMY", "idType": "TICKER"}}
        ]}
        """;

    assertThatThrownBy(() -> mockMvc.perform(
        post(BASE_PATH + "/trailing-total-returns")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)))
        .hasCauseInstanceOf(CalculationException.class)
        .hasMessageContaining("Metric mismatch");
  }

  @Test
  void shouldReturn415_whenContentTypeIsNotJson() throws Exception {
    mockMvc.perform(
        post(BASE_PATH + "/trailing-total-returns")
            .contentType(MediaType.TEXT_PLAIN)
            .content("not json"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @SuppressWarnings("unchecked")
  private CalculationService<?, ?> createMockService(CalculationMetric metric) {
    CalculationService mock = mock(CalculationService.class);
    lenient().when(mock.getMetric()).thenReturn(metric);
    lenient().when(mock.perform(any())).thenReturn(new BaseCalculationResult() {});
    mockServices.put(metric, mock);
    return mock;
  }

  static Stream<Arguments> calculationMetricArguments() {
    return CalculationTestDataProvider.calculationMetricArguments();
  }

  @Nested
  class ValidationIntegration {

    private MockMvc validatingMockMvc;
    private ObjectMapper om;

    @BeforeEach
    void setUp() {
      om = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      List<RequestValidator> validators = List.of(
          new PeriodReqValidator(),
          new CipsdGreaterThanCpedReqValidator(),
          new HoldingReqValidator());
      var facade = new RequestValidationFacade(validators);

      List<CalculationService<?, ?>> services = new java.util.ArrayList<>();
      for (CalculationMetric m : CalculationMetric.values()) {
        CalculationService svc = mock(CalculationService.class);
        lenient().when(svc.getMetric()).thenReturn(m);
        Object result = switch (m) {
          case BEST_WORST_PERIODS -> new com.fintex.ce.model.domain.result.period.BestWorstPeriodsResult();
          case DISTRIBUTION_OF_MONTHLY_RETURNS ->
            new com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult();
          default -> new BaseCalculationResult() {};
        };
        lenient().when(svc.perform(any())).thenReturn((BaseCalculationResult) result);
        services.add(svc);
      }

      var controller = new PortfolioCalculationController(services, facade);
      LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
      validator.afterPropertiesSet();
      validatingMockMvc = MockMvcBuilders.standaloneSetup(controller)
          .setControllerAdvice(new GlobalExceptionHandler())
          .setMessageConverters(new MappingJackson2HttpMessageConverter(om))
          .setValidator(validator)
          .build();
    }

    @Test
    void shouldReturnBadRequest_whenPeriodIsInvalid() throws Exception {
      PeriodCommand cmd = new PeriodCommand();
      cmd.setMetric(CalculationMetric.STANDARD_DEVIATION);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of("INVALID_PERIOD"));

      validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenCipsdIsAfterCped() throws Exception {
      PeriodCommand cmd = new PeriodCommand();
      cmd.setMetric(CalculationMetric.SHARPE_RATIO);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of("12"));
      cmd.setCustomIntervalPsd(LocalDate.of(2025, 12, 31));
      cmd.setCustomPed(LocalDate.of(2024, 1, 31));

      validatingMockMvc.perform(
          post(BASE_PATH + "/sharpe-ratio")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenCashHoldingHasNullCurrency() throws Exception {
      PortfolioHolding cashHolding = CashHolding.builder()
          .value(BigDecimal.valueOf(100))
          .holdingType(FinancialInstrumentType.CASH)
          .build();

      PeriodCommand cmd = new PeriodCommand();
      cmd.setMetric(CalculationMetric.TRAILING_TOTAL_RETURNS);
      cmd.setCurrency(Currency.CAD);
      cmd.setPeriods(Set.of("12"));
      cmd.setHoldings(List.of(cashHolding));

      validatingMockMvc.perform(
          post(BASE_PATH + "/trailing-total-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenBestWorstPeriodExceedsLimit() throws Exception {
      BestWorstPeriodsCommand cmd = new BestWorstPeriodsCommand();
      cmd.setMetric(CalculationMetric.BEST_WORST_PERIODS);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of());
      cmd.setCustomPsd(LocalDate.of(2024, 1, 31));
      cmd.setCustomPed(LocalDate.of(2024, 12, 31));
      cmd.setBestWorstTimeIntervalPeriods(Set.of(301L));

      validatingMockMvc.perform(
          post(BASE_PATH + "/best-worst-periods")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("BWP-002")));
    }

    @Test
    void shouldReturnBadRequest_whenCustomNumberOfBinsBelowMinimum() throws Exception {
      DistributionOfReturnsCommand cmd = new DistributionOfReturnsCommand();
      cmd.setMetric(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of("12"));
      cmd.setCustomNumberOfBins(4);

      validatingMockMvc.perform(
          post(BASE_PATH + "/distribution-of-monthly-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("DIS-001")));
    }

    @Test
    void shouldReturnBadRequest_whenCustomNumberOfBinsAboveMaximum() throws Exception {
      DistributionOfReturnsCommand cmd = new DistributionOfReturnsCommand();
      cmd.setMetric(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of("12"));
      cmd.setCustomNumberOfBins(31);

      validatingMockMvc.perform(
          post(BASE_PATH + "/distribution-of-monthly-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("DIS-002")));
    }

    @Test
    void shouldReturnBadRequest_whenIncomeForecastTimeIntervalIsZero() throws Exception {
      IncomeForecastCommand cmd = new IncomeForecastCommand();
      cmd.setMetric(CalculationMetric.INCOME_FORECAST);
      cmd.setTimeIntervalPeriods(0);

      validatingMockMvc.perform(
          post(BASE_PATH + "/income-forecast")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("TIP-003")));
    }

    @Test
    void shouldReturnBadRequest_whenIncomeForecastTimeIntervalIsNegative() throws Exception {
      IncomeForecastCommand cmd = new IncomeForecastCommand();
      cmd.setMetric(CalculationMetric.INCOME_FORECAST);
      cmd.setTimeIntervalPeriods(-12);

      validatingMockMvc.perform(
          post(BASE_PATH + "/income-forecast")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("TIP-003")));
    }

    @Test
    void shouldReturnBadRequest_whenBestWorstPeriodIsZero() throws Exception {
      BestWorstPeriodsCommand cmd = new BestWorstPeriodsCommand();
      cmd.setMetric(CalculationMetric.BEST_WORST_PERIODS);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of());
      cmd.setCustomPsd(LocalDate.of(2024, 1, 31));
      cmd.setCustomPed(LocalDate.of(2024, 12, 31));
      cmd.setBestWorstTimeIntervalPeriods(Set.of(0L));

      validatingMockMvc.perform(
          post(BASE_PATH + "/best-worst-periods")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("BWP-001")));
    }

    @Test
    void shouldPassValidation_whenBestWorstPeriodIsAtBoundaryValues() throws Exception {
      BestWorstPeriodsCommand cmd = new BestWorstPeriodsCommand();
      cmd.setMetric(CalculationMetric.BEST_WORST_PERIODS);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of(dummyHolding()));
      cmd.setCustomPsd(LocalDate.of(2024, 1, 31));
      cmd.setCustomPed(LocalDate.of(2024, 12, 31));
      cmd.setBestWorstTimeIntervalPeriods(Set.of(1L, 300L));

      validatingMockMvc.perform(
          post(BASE_PATH + "/best-worst-periods")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isOk());
    }

    @Test
    void shouldPassValidation_whenNumberOfBinsIsAtBoundaryValues() throws Exception {
      DistributionOfReturnsCommand lowerBoundCmd = new DistributionOfReturnsCommand();
      lowerBoundCmd.setMetric(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS);
      lowerBoundCmd.setCurrency(Currency.CAD);
      lowerBoundCmd.setHoldings(List.of(dummyHolding()));
      lowerBoundCmd.setPeriods(Set.of("12"));
      lowerBoundCmd.setCustomNumberOfBins(5);

      validatingMockMvc.perform(
          post(BASE_PATH + "/distribution-of-monthly-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(lowerBoundCmd)))
          .andExpect(status().isOk());

      DistributionOfReturnsCommand upperBoundCmd = new DistributionOfReturnsCommand();
      upperBoundCmd.setMetric(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS);
      upperBoundCmd.setCurrency(Currency.CAD);
      upperBoundCmd.setHoldings(List.of(dummyHolding()));
      upperBoundCmd.setPeriods(Set.of("12"));
      upperBoundCmd.setCustomNumberOfBins(30);

      validatingMockMvc.perform(
          post(BASE_PATH + "/distribution-of-monthly-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(upperBoundCmd)))
          .andExpect(status().isOk());
    }

    private static PortfolioHolding dummyHolding() {
      return new PortfolioHolding(
          BigDecimal.ONE, FinancialInstrumentType.MUTUAL_FUND_CANADA,
          new SecurityIdentifier("DUMMY", FiIdentifierType.TICKER));
    }

    @Test
    void shouldReturnBadRequest_whenPortfolioCommandCurrencyIsMissing() throws Exception {
      PeriodCommand cmd = new PeriodCommand();
      cmd.setMetric(CalculationMetric.SHARPE_RATIO);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of("12"));

      validatingMockMvc.perform(
          post(BASE_PATH + "/sharpe-ratio")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("must not be null")));
    }

    @Test
    void shouldReturnBadRequest_whenReturnCommandCurrencyIsMissing() throws Exception {
      ReturnCommand cmd = new ReturnCommand();
      cmd.setMetric(CalculationMetric.ANNUAL_RETURNS);
      cmd.setHoldings(List.of());

      validatingMockMvc.perform(
          post(BASE_PATH + "/annual-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("must not be null")));
    }

    @Test
    void shouldReturnAllViolations_whenMultipleJakartaConstraintsFailOnOneRequest() throws Exception {
      DistributionOfReturnsCommand cmd = new DistributionOfReturnsCommand();
      cmd.setMetric(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of("12"));
      cmd.setCustomNumberOfBins(999);

      validatingMockMvc.perform(
          post(BASE_PATH + "/distribution-of-monthly-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("DIS-002")))
          .andExpect(content().string(
              org.hamcrest.Matchers.containsString("must not be null")));
    }
  }
}
