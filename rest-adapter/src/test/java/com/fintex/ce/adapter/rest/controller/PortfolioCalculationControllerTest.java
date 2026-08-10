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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.fintex.ce.adapter.rest.controller.PortfolioCalculationController.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private EnumMap<CalculationMetric, CalculationService<?, ?, ?>> mockServices;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    mockServices = new EnumMap<>(CalculationMetric.class);
    List<CalculationService<?, ?, ?>> serviceList = Arrays.stream(CalculationMetric.values())
        .<CalculationService<?, ?, ?>>map(this::createMockService)
        .toList();

    var controller = controller(serviceList, new RequestValidationFacade(List.of()),
        new RecordingCalculationObservability());

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("calculationMetricArguments")
  void shouldReturnMappedResponse_whenValidMetricRequested(
      CalculationMetric metric,
      CalculationCommand command,
      Object serviceResult,
      Class<? extends BaseCalculationResult> responseType) throws Exception {
    stubCalculationResult(mockServices.get(metric), (BaseCalculationResult) serviceResult);

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

    verify(mockServices.get(metric)).perform(any(), any());

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
          {"value": 1, "holdingType": "MUTUAL_FUND", "country": "CANADA",
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
          {"value": 1, "holdingType": "MUTUAL_FUND", "country": "CANADA",
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

  private CalculationService<?, ?, ?> createMockService(CalculationMetric metric) {
    CalculationService<?, ?, ?> mock = mock(CalculationService.class);
    lenient().when(mock.getMetric()).thenReturn(metric);
    lenient().when(mock.requiredAttributes()).thenReturn(List.of());
    stubCalculationResult(mock, new BaseCalculationResult() {});
    mockServices.put(metric, mock);
    return mock;
  }

  static Stream<Arguments> calculationMetricArguments() {
    return CalculationTestDataProvider.calculationMetricArguments();
  }

  private static PortfolioCalculationController controller(List<CalculationService<?, ?, ?>> services,
      RequestValidationFacade validationFacade, CalculationObservability observability) {
    SecurityAttributesFetcher fetcher = mock(SecurityAttributesFetcher.class);
    lenient().when(fetcher.fetch(any(), anyCollection(), any())).thenReturn(SecurityData.EMPTY);
    lenient().when(fetcher.fetch(any(), any(CompositeSecurityAttribute.class), any())).thenReturn(Map.of());
    CalculationOrchestrator orchestrator = new MetricCalculationOrchestrator(
        services,
        fetcher,
        new DefaultDataProperties(List.of(DataProvider.MORNINGSTAR, DataProvider.FMP)),
        CalculationDurationRecorder.NO_OP);
    LocalValidatorFactoryBean beanValidator = new LocalValidatorFactoryBean();
    beanValidator.afterPropertiesSet();
    return new PortfolioCalculationController(orchestrator, validationFacade, observability, beanValidator);
  }

  /**
   * Records what the controller actually handed over to be observed. The per-metric numbers themselves are the
   * observability adapter's business and are covered there; what has to be pinned here is the boundary — which requests
   * reach the port at all, and with which outcome.
   */
  private static final class RecordingCalculationObservability implements CalculationObservability {

    private final List<String> observedMetrics = new ArrayList<>();
    private final List<String> failedMetrics = new ArrayList<>();

    @Override
    public BaseCalculationResult observe(
        String metricName,
        CalculationCommand command,
        Supplier<BaseCalculationResult> action) {
      observedMetrics.add(metricName);
      try {
        return action.get();
      } catch (RuntimeException exception) {
        failedMetrics.add(metricName);
        throw exception;
      }
    }

    @Override
    public CompositeCalculationResult observeComposite(
        List<CalculationCommand> commands,
        Supplier<CompositeCalculationResult> action) {
      commands.forEach(command -> observedMetrics.add(command.getMetric().getValue()));
      return action.get();
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void verifyCalculationPerformed(CalculationService<?, ?, ?> service,
      org.mockito.ArgumentCaptor<CalculationCommand> commandCaptor) {
    verify((CalculationService) service).perform(commandCaptor.capture(), any());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void stubCalculationResult(CalculationService<?, ?, ?> service, BaseCalculationResult result) {
    lenient().when(((CalculationService) service).perform(any(), any())).thenReturn(result);
  }

  @Nested
  class CompositeEndpoint {

    @Test
    void shouldReturnResultsPerMetric_whenSeveralMetricsRequested() throws Exception {
      String requestBody = """
          {"currency": "CAD",
           "holdings": [
             {"value": 1, "holdingType": "MUTUAL_FUND", "country": "CANADA",
              "securityIdentifier": {"id": "DUMMY", "idType": "TICKER"}}
           ],
           "commands": [
             {"metric": "sharpe-ratio", "timeIntervalPeriods": ["12"]},
             {"metric": "asset-allocations"}
           ]}
          """;

      mockMvc.perform(
          post(BASE_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().string(org.hamcrest.Matchers.containsString("results")));

      verify(mockServices.get(CalculationMetric.SHARPE_RATIO)).perform(any(), any());
      verify(mockServices.get(CalculationMetric.ASSET_ALLOCATIONS)).perform(any(), any());
    }

    @Test
    void shouldPropagateSharedInputsToCommands_whenCommandsOmitThem() throws Exception {
      String requestBody = """
          {"currency": "CAD",
           "dataProviders": ["MORNINGSTAR"],
           "holdings": [
             {"value": 1, "holdingType": "MUTUAL_FUND", "country": "CANADA",
              "securityIdentifier": {"id": "SHARED", "idType": "TICKER"}}
           ],
           "commands": [
             {"metric": "sharpe-ratio", "timeIntervalPeriods": ["12"]}
           ]}
          """;

      mockMvc.perform(
          post(BASE_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk());

      org.mockito.ArgumentCaptor<CalculationCommand> captor = org.mockito.ArgumentCaptor
          .forClass(CalculationCommand.class);
      verifyCalculationPerformed(mockServices.get(CalculationMetric.SHARPE_RATIO), captor);
      PeriodCommand executed = (PeriodCommand) captor.getValue();
      assertThat(executed.getHoldings())
          .singleElement()
          .satisfies(holding -> assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("SHARED"));
      assertThat(executed.getCurrency()).isEqualTo(Currency.CAD);
      assertThat(executed.getDataProviders()).containsExactly(DataProvider.MORNINGSTAR);
    }

    @Test
    void shouldPreferCommandValues_whenCommandOverridesSharedInputs() throws Exception {
      String requestBody = """
          {"currency": "CAD",
           "holdings": [
             {"value": 1, "holdingType": "MUTUAL_FUND", "country": "CANADA",
              "securityIdentifier": {"id": "SHARED", "idType": "TICKER"}}
           ],
           "commands": [
             {"metric": "sharpe-ratio", "currency": "USD", "timeIntervalPeriods": ["12"], "holdings": [
               {"value": 1, "holdingType": "MUTUAL_FUND", "country": "CANADA",
                "securityIdentifier": {"id": "OWN", "idType": "TICKER"}}
             ]}
           ]}
          """;

      mockMvc.perform(
          post(BASE_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk());

      org.mockito.ArgumentCaptor<CalculationCommand> captor = org.mockito.ArgumentCaptor
          .forClass(CalculationCommand.class);
      verifyCalculationPerformed(mockServices.get(CalculationMetric.SHARPE_RATIO), captor);
      PeriodCommand executed = (PeriodCommand) captor.getValue();
      assertThat(executed.getHoldings())
          .singleElement()
          .satisfies(holding -> assertThat(holding.getSecurityIdentifier().getId()).isEqualTo("OWN"));
      assertThat(executed.getCurrency()).isEqualTo(Currency.USD);
    }

    @Test
    void shouldThrowException_whenDuplicateMetricRequested() {
      String requestBody = """
          {"currency": "CAD",
           "holdings": [
             {"value": 1, "holdingType": "MUTUAL_FUND", "country": "CANADA",
              "securityIdentifier": {"id": "DUMMY", "idType": "TICKER"}}
           ],
           "commands": [
             {"metric": "sharpe-ratio"},
             {"metric": "sharpe-ratio"}
           ]}
          """;

      assertThatThrownBy(() -> mockMvc.perform(
          post(BASE_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody)))
          .hasCauseInstanceOf(CalculationException.class)
          .hasMessageContaining("Duplicate calculation metric");
    }

    @Test
    void shouldThrowException_whenCommandHasNoMetric() {
      String requestBody = """
          {"currency": "CAD",
           "holdings": [
             {"value": 1, "holdingType": "MUTUAL_FUND", "country": "CANADA",
              "securityIdentifier": {"id": "DUMMY", "idType": "TICKER"}}
           ],
           "commands": [
             {"timeIntervalPeriods": ["12"]}
           ]}
          """;

      assertThatThrownBy(() -> mockMvc.perform(
          post(BASE_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody)))
          .hasCauseInstanceOf(CalculationException.class);
    }
  }

  @Nested
  class ValidationIntegration {

    private MockMvc validatingMockMvc;
    private ObjectMapper om;
    private Map<CalculationMetric, CalculationService<?, ?, ?>> calculationServices;
    private RecordingCalculationObservability observability;

    @BeforeEach
    void setUp() {
      om = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      List<RequestValidator> validators = List.of(
          new TwelveMonthMinimumPeriodsReqValidator(),
          new TrailingPeriodsReqValidator(),
          new StandardDeviationPeriodsReqValidator(),
          new CipsdGreaterThanCpedReqValidator(),
          new HoldingReqValidator(new HoldingsValidator(new HoldingsValidationProperties())));
      var facade = new RequestValidationFacade(validators);

      List<CalculationService<?, ?, ?>> services = new java.util.ArrayList<>();
      calculationServices = new EnumMap<>(CalculationMetric.class);
      for (CalculationMetric m : CalculationMetric.values()) {
        CalculationService<?, ?, ?> svc = mock(CalculationService.class);
        lenient().when(svc.getMetric()).thenReturn(m);
        lenient().when(svc.requiredAttributes()).thenReturn(List.of());
        Object result = switch (m) {
          case BEST_WORST_PERIODS -> new com.fintex.ce.model.domain.result.period.BestWorstPeriodsResult();
          case DISTRIBUTION_OF_MONTHLY_RETURNS ->
            new com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult();
          default -> new BaseCalculationResult() {};
        };
        stubCalculationResult(svc, (BaseCalculationResult) result);
        services.add(svc);
        calculationServices.put(m, svc);
      }

      observability = new RecordingCalculationObservability();
      var controller = controller(services, facade, observability);
      LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
      validator.afterPropertiesSet();
      validatingMockMvc = MockMvcBuilders.standaloneSetup(controller)
          .setControllerAdvice(new GlobalExceptionHandler())
          .setMessageConverters(new MappingJackson2HttpMessageConverter(om))
          .setValidator(validator)
          .build();
    }

    /**
     * Posted as raw JSON on purpose: a period is now a typed enum, so an unusable value cannot be put on the command
     * object at all and this rejection has moved into deserialization. Raw JSON is the only way to exercise the path a
     * real caller would take.
     */
    @Test
    void shouldReturnBadRequest_whenPeriodIsNotAKnownPeriod() throws Exception {
      String body = """
          {"metric":"STANDARD_DEVIATION","currency":"CAD","holdings":[],
           "timeIntervalPeriods":["INVALID_PERIOD"]}""";

      validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest());
    }

    /** A real period, but shorter than the twelve months a standard deviation needs — rejected by the subset check. */
    @Test
    void shouldReturnBadRequest_whenPeriodIsTooShortForTheMetric() throws Exception {
      PeriodCommand cmd = new PeriodCommand();
      cmd.setMetric(CalculationMetric.STANDARD_DEVIATION);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of(TimePeriod.SIX_MTH));

      validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNullStandardDeviation_whenPeriodIsLessThanTwelveMonths() throws Exception {
      PeriodCommand command = new PeriodCommand();
      command.setMetric(CalculationMetric.STANDARD_DEVIATION);
      command.setCurrency(Currency.CAD);
      command.setHoldings(List.of(dummyHolding()));
      command.setPeriods(Set.of(TimePeriod.SIX_MTH));
      StandardDeviationResult result = new StandardDeviationResult(Set.of(new TimeIntervalResult(TimePeriod.SIX_MTH
          .name(),
          null)));
      stubCalculationResult(calculationServices.get(CalculationMetric.STANDARD_DEVIATION), result);

      MvcResult mvcResult = validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(command)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andReturn();

      StandardDeviationResult actual = om.readValue(mvcResult.getResponse().getContentAsString(),
          StandardDeviationResult.class);

      assertThat(actual.getStandardDeviation())
          .containsExactlyInAnyOrder(new TimeIntervalResult(TimePeriod.SIX_MTH.name(), null));

      verify(calculationServices.get(CalculationMetric.STANDARD_DEVIATION)).perform(any(), any());
    }

    @Test
    void shouldReturnBadRequest_whenStandardDeviationPeriodIsYearToDate() throws Exception {
      PeriodCommand command = new PeriodCommand();
      command.setMetric(CalculationMetric.STANDARD_DEVIATION);
      command.setCurrency(Currency.CAD);
      command.setHoldings(List.of(dummyHolding()));
      command.setPeriods(Set.of(TimePeriod.YTD));

      validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(command)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(org.hamcrest.Matchers.containsString("TIP-011")))
          .andExpect(content().string(org.hamcrest.Matchers.containsString(
              "Time interval period 'YTD' is not supported")));

      verify(calculationServices.get(CalculationMetric.STANDARD_DEVIATION), org.mockito.Mockito.never())
          .perform(any(), any());
    }

    /**
     * The per-metric statistics describe calculations, not HTTP traffic. A request rejected before dispatch never
     * reached a calculator, so handing it to the observability port would let malformed input raise the failure rate of
     * a metric that is working. Both halves are asserted together because a controller that never observes anything
     * would satisfy the first half on its own.
     */
    @Test
    void shouldObserveOnlyDispatchedCalculations_whenARequestIsRejectedBeforeTheCalculationRuns() throws Exception {
      PeriodCommand rejected = new PeriodCommand();
      rejected.setMetric(CalculationMetric.STANDARD_DEVIATION);
      rejected.setCurrency(Currency.CAD);
      rejected.setHoldings(List.of(dummyHolding()));
      rejected.setPeriods(Set.of(TimePeriod.YTD));

      validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(rejected)))
          .andExpect(status().isBadRequest());

      assertThat(observability.observedMetrics)
          .as("a rejected request is not a calculation execution")
          .isEmpty();

      PeriodCommand dispatched = new PeriodCommand();
      dispatched.setMetric(CalculationMetric.SHARPE_RATIO);
      dispatched.setCurrency(Currency.CAD);
      dispatched.setHoldings(List.of(dummyHolding()));
      dispatched.setPeriods(Set.of(TimePeriod.ONE_YR));
      doThrow(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.toException("Security Master"))
          .when(calculationServices.get(CalculationMetric.SHARPE_RATIO)).perform(any(), any());

      validatingMockMvc.perform(
          post(BASE_PATH + "/sharpe-ratio")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(dispatched)))
          .andExpect(status().isServiceUnavailable());

      assertThat(observability.observedMetrics)
          .as("a calculation that ran and failed is exactly what the port is there to see")
          .containsExactly(CalculationMetric.SHARPE_RATIO.getValue());
      assertThat(observability.failedMetrics)
          .as("the failure has to reach the port, otherwise it lands in no failure count at all")
          .containsExactly(CalculationMetric.SHARPE_RATIO.getValue());
    }

    @Test
    void shouldReturnBadRequest_whenCipsdIsAfterCped() throws Exception {
      PeriodCommand cmd = new PeriodCommand();
      cmd.setMetric(CalculationMetric.SHARPE_RATIO);
      cmd.setCurrency(Currency.CAD);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of(TimePeriod.ONE_YR));
      cmd.setCustomIntervalPsd(LocalDate.of(2025, 12, 31));
      cmd.setCustomPed(LocalDate.of(2024, 1, 31));

      validatingMockMvc.perform(
          post(BASE_PATH + "/sharpe-ratio")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnCipsdOutsideDataRangeError_whenStandardDeviationCipsdIsAfterPortfolioPerformanceEndDate()
        throws Exception {
      LocalDate cipsd = LocalDate.of(2025, 10, 31);
      LocalDate performanceStartDate = LocalDate.of(1998, 5, 31);
      LocalDate performanceEndDate = LocalDate.of(2025, 9, 30);
      PeriodCommand command = new PeriodCommand();
      command.setMetric(CalculationMetric.STANDARD_DEVIATION);
      command.setCurrency(Currency.CAD);
      command.setHoldings(List.of(dummyHolding()));
      command.setPeriods(Set.of(TimePeriod.ONE_YR));
      command.setCustomIntervalPsd(cipsd);
      doThrow(ErrorCode.CIPSD_OUTSIDE_DATA_RANGE_ERROR.toException(cipsd, performanceStartDate, performanceEndDate))
          .when(calculationServices.get(CalculationMetric.STANDARD_DEVIATION)).perform(any(), any());

      validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(command)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.notifications[0].code").value("RET-016"))
          .andExpect(jsonPath("$.notifications[0].severity").value("ERROR"))
          .andExpect(jsonPath("$.notifications[0].message").value(
              "CIPSD 2025-10-31 is outside the available monthly returns range [1998-05-31, 2025-09-30]"))
          .andExpect(jsonPath("$.notifications[0].metadata['param-1']").value("2025-10-31"))
          .andExpect(jsonPath("$.notifications[0].metadata['param-2']").value("1998-05-31"))
          .andExpect(jsonPath("$.notifications[0].metadata['param-3']").value("2025-09-30"));

      verify(calculationServices.get(CalculationMetric.STANDARD_DEVIATION)).perform(any(), any());
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
      cmd.setPeriods(Set.of(TimePeriod.ONE_YR));
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
      cmd.setPeriods(Set.of(TimePeriod.ONE_YR));
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
      cmd.setPeriods(Set.of(TimePeriod.ONE_YR));
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
      lowerBoundCmd.setPeriods(Set.of(TimePeriod.ONE_YR));
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
      upperBoundCmd.setPeriods(Set.of(TimePeriod.ONE_YR));
      upperBoundCmd.setCustomNumberOfBins(30);

      validatingMockMvc.perform(
          post(BASE_PATH + "/distribution-of-monthly-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(upperBoundCmd)))
          .andExpect(status().isOk());
    }

    private static PortfolioHolding dummyHolding() {
      return new PortfolioHolding(
          BigDecimal.ONE, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
          new SecurityIdentifier("DUMMY", FiIdentifierType.TICKER));
    }

    @Test
    void shouldReturnBadRequest_whenPortfolioCommandCurrencyIsMissing() throws Exception {
      PeriodCommand cmd = new PeriodCommand();
      cmd.setMetric(CalculationMetric.SHARPE_RATIO);
      cmd.setHoldings(List.of());
      cmd.setPeriods(Set.of(TimePeriod.ONE_YR));

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
      cmd.setPeriods(Set.of(TimePeriod.ONE_YR));
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
