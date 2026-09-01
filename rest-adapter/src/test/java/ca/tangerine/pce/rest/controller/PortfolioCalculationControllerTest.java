package ca.tangerine.pce.rest.controller;

import ca.tangerine.pce.application.calculation.orchestration.MetricCalculationOrchestrator;
import ca.tangerine.pce.application.config.DefaultDataProperties;
import ca.tangerine.pce.calculation.CalculationOrchestrator;
import ca.tangerine.pce.calculation.CalculationService;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.result.composite.CompositeCalculationResult;
import ca.tangerine.pce.model.domain.result.risk.StandardDeviationResult;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.ReturnCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.CalculationException;
import ca.tangerine.pce.port.observability.CalculationDurationRecorder;
import ca.tangerine.pce.port.observability.CalculationObservability;
import ca.tangerine.pce.port.webclient.mic.SecurityAttributesFetcher;
import ca.tangerine.pce.rest.validation.RequestValidationFacade;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.pce.rest.validation.validators.CipsdGreaterThanCpedReqValidator;
import ca.tangerine.pce.rest.validation.validators.HoldingReqValidator;
import ca.tangerine.pce.rest.validation.validators.HoldingsValidationProperties;
import ca.tangerine.pce.rest.validation.validators.HoldingsValidator;
import ca.tangerine.pce.rest.validation.validators.StandardDeviationPeriodsReqValidator;
import ca.tangerine.pce.rest.validation.validators.TrailingPeriodsReqValidator;
import ca.tangerine.pce.rest.validation.validators.TwelveMonthMinimumPeriodsReqValidator;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;
import ca.tangerine.wm.commons.domain.holding.HoldingType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.error.ErrorResponse;
import ca.tangerine.wm.commons.error.Severity;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static ca.tangerine.pce.rest.controller.PortfolioCalculationController.BASE_PATH;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.cash;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
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
        stubCalculationResult(svc, new BaseCalculationResult() {});
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
     *
     * <p>
     * The metric carries its canonical value rather than the constant name, because the discriminator is resolved
     * through {@code CalculationMetric.from} and a name it does not know is itself a 400 — which would have made this
     * pass without the period ever being read. The payload is asserted for the same reason: the status alone cannot say
     * which value was rejected, and an empty holdings list answers 400 as well.
     *
     * <p>
     * {@code TimePeriod} accepts a member name or a month count through its own {@code @JsonCreator}, so an unusable
     * value arrives as a creator failure rather than as the enum mismatch that carries the field and the allowed set —
     * hence the generic unreadable-body code here, unlike the accumulate-types case below.
     */
    @Test
    void shouldReturnBadRequest_whenPeriodIsNotAKnownPeriod() throws Exception {
      String body = """
          {"metric":"standard-deviation","currency":"CAD","holdings":[],
           "timeIntervalPeriods":["INVALID_PERIOD"]}""";

      MvcResult result = validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest())
          .andReturn();

      ErrorResponse error = om.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
      assertThat(error.getNotifications()).singleElement().satisfies(notification -> {
        assertThat(notification.getCode()).isEqualTo(ErrorCode.Codes.BAD_INPUT);
        assertThat(notification.getMessage()).isEqualTo(ErrorCode.BAD_INPUT.getMessage());
      });
    }

    /**
     * The rejection that replaced the deleted "at most twelve accumulate types" check: the field is typed on
     * {@link HoldingType}, so a code outside the provider's vocabulary never reaches the aggregation, where it used to
     * match nothing and silently change the answer. Posted as raw JSON because a value that cannot be put on the
     * command object is exactly what this path is about, and the whole payload is asserted — the code, the message
     * naming the field and the accepted values, and the field itself — since a caller that cannot tell which field it
     * got wrong is the reason this is not the generic bad-input answer.
     */
    @Test
    void shouldReturnBadRequestNamingTheField_whenAccumulateHoldingTypeIsNotAHoldingTypeCode() throws Exception {
      String body = """
          {"metric":"top-common-holdings","currency":"CAD","holdings":[],
           "accumulateHoldingTypes":["NOT_A_CODE"]}""";

      MvcResult result = validatingMockMvc.perform(
          post(BASE_PATH + "/top-common-holdings")
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest())
          .andReturn();

      ErrorResponse error = om.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
      assertThat(error.getNotifications()).singleElement().satisfies(notification -> {
        assertThat(notification.getCode()).isEqualTo(ErrorCode.Codes.FIELD_VALUE_NOT_ALLOWED);
        assertThat(notification.getFieldName()).isEqualTo("accumulateHoldingTypes");
        assertThat(notification.getMessage())
            .startsWith("accumulateHoldingTypes must be one of: ")
            // Constant names, not vendor codes: what a caller sends is the name, not the provider's code.
            .contains(HoldingType.E.name(), HoldingType.BT.name());
        assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
      });
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
      command.setHoldings(List.of(holding("DUMMY", FiIdentifierType.TICKER,
          FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.ONE)));
      command.setPeriods(Set.of(TimePeriod.SIX_MTH));
      Map<String, BigDecimal> standardDeviation = new LinkedHashMap<>();
      standardDeviation.put(TimePeriod.SIX_MTH.name(), null);
      StandardDeviationResult result = new StandardDeviationResult(standardDeviation);
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
          .containsOnlyKeys(TimePeriod.SIX_MTH.name())
          .containsEntry(TimePeriod.SIX_MTH.name(), null);

      verify(calculationServices.get(CalculationMetric.STANDARD_DEVIATION)).perform(any(), any());
    }

    @Test
    void shouldReturnBadRequest_whenStandardDeviationPeriodIsYearToDate() throws Exception {
      PeriodCommand command = new PeriodCommand();
      command.setMetric(CalculationMetric.STANDARD_DEVIATION);
      command.setCurrency(Currency.CAD);
      command.setHoldings(List.of(holding("DUMMY", FiIdentifierType.TICKER,
          FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.ONE)));
      command.setPeriods(Set.of(TimePeriod.YTD));

      validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(command)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(org.hamcrest.Matchers.containsString("TIP-004")))
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
      rejected.setHoldings(List.of(holding("DUMMY", FiIdentifierType.TICKER,
          FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.ONE)));
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
      dispatched.setHoldings(List.of(holding("DUMMY", FiIdentifierType.TICKER,
          FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.ONE)));
      dispatched.setPeriods(Set.of(TimePeriod.ONE_YR));
      doThrow(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.toException("Market Investment Catalogue"))
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
      command.setHoldings(List.of(holding("DUMMY", FiIdentifierType.TICKER,
          FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.ONE)));
      command.setPeriods(Set.of(TimePeriod.ONE_YR));
      command.setCustomIntervalPsd(cipsd);
      doThrow(ErrorCode.CIPSD_OUTSIDE_DATA_RANGE_ERROR.toException(cipsd, performanceStartDate, performanceEndDate))
          .when(calculationServices.get(CalculationMetric.STANDARD_DEVIATION)).perform(any(), any());

      validatingMockMvc.perform(
          post(BASE_PATH + "/standard-deviation")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(command)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.notifications[0].code").value("RET-012"))
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
      PortfolioHolding cashHolding = cash(null, BigDecimal.valueOf(100));

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
    void shouldReturnBadRequest_whenPortfolioHoldingsAreEmpty() throws Exception {
      PeriodCommand cmd = new PeriodCommand();
      cmd.setMetric(CalculationMetric.TRAILING_TOTAL_RETURNS);
      cmd.setCurrency(Currency.CAD);
      cmd.setPeriods(Set.of(TimePeriod.ONE_YR));
      cmd.setHoldings(List.of());
      // Any portfolio-based endpoint would exercise the same @NotEmpty holdings validation.
      // trailing-total-returns is used here only as a representative endpoint.
      validatingMockMvc.perform(
          post(BASE_PATH + "/trailing-total-returns")
              .contentType(MediaType.APPLICATION_JSON)
              .content(om.writeValueAsString(cmd)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.notifications[0].code")
              .value(ErrorCode.Codes.FIELD_NOT_EMPTY))
          .andExpect(jsonPath("$.notifications[0].message")
              .value("holdings must not be empty"))
          .andExpect(jsonPath("$.notifications[0].fieldName")
              .value("holdings"));
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

  }
}
