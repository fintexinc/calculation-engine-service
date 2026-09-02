package ca.tangerine.pce.e2e;

import ca.tangerine.pce.PortfolioCalculationEngineApplication;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.GeographicExposureResult;
import ca.tangerine.pce.model.domain.result.fee.ManagementFeeResult;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.CompositeCalculationRequest;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.performance.MonthlyReturns;
import ca.tangerine.wm.commons.domain.value.DateBigDecimalValue;
import ca.tangerine.wm.commons.error.ErrorResponse;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ca.tangerine.pce.e2e.AbstractPortfolioCalculationE2ETest.parseJson;
import static ca.tangerine.pce.e2e.AbstractPortfolioCalculationE2ETest.readJson;
import static ca.tangerine.pce.e2e.AbstractPortfolioCalculationE2ETest.writeJson;
import static ca.tangerine.pce.e2e.E2EPortfolios.etf;
import static ca.tangerine.pce.e2e.E2EPortfolios.fund;
import static ca.tangerine.pce.e2e.MicAttributeResponses.attributeResult;
import static ca.tangerine.pce.e2e.MicAttributeResponses.attributesDispatcher;
import static ca.tangerine.pce.e2e.MicAttributeResponses.geographicAllocationRow;
import static ca.tangerine.pce.e2e.MicAttributeResponses.geographyRow;
import static ca.tangerine.pce.e2e.MicAttributeResponses.morningstarOnly;
import static ca.tangerine.pce.e2e.MicAttributeResponses.regionValue;
import static ca.tangerine.pce.e2e.MicFeeResponses.currencyOnlyRow;
import static ca.tangerine.pce.e2e.MicFeeResponses.managementFeeRow;
import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;

/**
 * End-to-end coverage for the composite endpoint — {@code POST /api/v1/portfolio/calculations} with no metric in the
 * path — which is how a client asks for the several metrics one screen needs in a single call. Until now it was only
 * exercised incidentally, by the statistics test counting what it had executed.
 *
 * <p>
 * Four things are its own, none of them visible from a single-metric request: the shared inputs declared once at the
 * top level reach every nested command, the attributes those metrics need are fetched together rather than once per
 * metric, a metric that cannot be calculated is reported beside the ones that could instead of discarding them, and a
 * request naming the same metric twice — or a command naming none — is rejected before any calculation starts.
 *
 * <p>
 * This is the one calculation test that cannot extend {@link AbstractPortfolioCalculationE2ETest}: that base posts to
 * {@code /{metricName}} and its five inherited scenarios are all about one metric. The Market Investment Catalogue mock
 * and its property registration are therefore repeated here — two methods, rather than bending the base's contract to a
 * request shape it does not describe.
 */
@Tag("e2e")
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "60s")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompositeCalculationE2ETest {

  private static final String COMPOSITE_PATH = "/api/v1/portfolio/calculations";
  private static final String CANADIAN_FUND = "F00000CMP1";
  private static final String WORLD_ETF = "XAW";
  private static final int MONTHS_OF_HISTORY = 24;

  private static MockWebServer micMockServer;

  @Autowired
  private WebTestClient webTestClient;

  @BeforeAll
  static void startMicMockServer() throws IOException {
    micMockServer = new MockWebServer();
    micMockServer.start();
  }

  @AfterAll
  static void shutdownMicMockServer() throws IOException {
    if (micMockServer != null) {
      micMockServer.shutdown();
      micMockServer = null;
    }
  }

  @DynamicPropertySource
  static void registerMarketInvestmentCatalogueBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.market-investment-catalogue.rest.base-url", () -> {
      if (micMockServer == null) {
        startMicMockServerQuietly();
      }
      return micMockServer.url("/").toString().replaceAll("/$", "");
    });
  }

  /**
   * Spring resolves the dynamic property while building the context, which can happen before {@code @BeforeAll} on a
   * cold JVM; the server is started on demand so the base URL is never registered against a null.
   */
  private static void startMicMockServerQuietly() {
    try {
      micMockServer = new MockWebServer();
      micMockServer.start();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Three metrics that between them need four Market Investment Catalogue attributes, and none of which carries
   * holdings, data providers or a currency of its own — all three come from the top level. The standard-deviation
   * command is what makes the currency half of that assertion real rather than decorative: its currency is
   * {@code @NotNull}, so if the shared one did not reach it the request would be rejected before any calculation ran.
   *
   * <p>
   * The attribute call is counted, because "fetched together in as few round trips as possible" is the endpoint's
   * documented promise and the reason a client uses it instead of three separate requests. One call for one provider
   * group, whatever the number of metrics asking.
   */
  @Test
  void shouldReturnEveryMetricAndFetchTheAttributesTogether_whenTheRequestDeclaresSharedInputs() {
    micMockServer.setDispatcher(attributesDispatcher(allAttributesBody(
        managementFeeRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, "1.00", Currency.CAD),
        managementFeeRow(WORLD_ETF, FiIdentifierType.TICKER, "2.00", Currency.CAD))));
    int requestsBefore = micMockServer.getRequestCount();

    var response = post(writeJson(CompositeCalculationRequest.builder()
        .holdings(sharedHoldings())
        .dataProviders(morningstarOnly())
        .currency(Currency.CAD)
        .commands(List.of(
            metricOnly(CalculationMetric.GEOGRAPHIC_EXPOSURE),
            metricOnly(CalculationMetric.MANAGEMENT_FEE),
            standardDeviationWithoutCurrency()))
        .build()));

    assertThat(response.status()).isEqualTo(HttpStatus.OK.value());
    JsonNode body = parseJson(response.responseBody());
    assertThat(body.path("failures")).isEmpty();
    assertThat(body.path("results").propertyNames()).containsExactlyInAnyOrder(
        CalculationMetric.GEOGRAPHIC_EXPOSURE.getValue(),
        CalculationMetric.MANAGEMENT_FEE.getValue(),
        CalculationMetric.STANDARD_DEVIATION.getValue());
    assertThat(micMockServer.getRequestCount() - requestsBefore)
        .as("every metric's attributes are fetched in one call for the shared provider group")
        .isEqualTo(1);

    GeographicExposureResult exposure = resultOf(body, CalculationMetric.GEOGRAPHIC_EXPOSURE,
        GeographicExposureResult.class);
    assertThat(exposure.getWarnings()).isEmpty();
    assertThat(exposure.getGeographicExposure().get(GeographicRegionType.US)).isEqualByComparingTo("0.60");
    assertThat(exposure.getGeographicExposure().get(GeographicRegionType.CANADA)).isEqualByComparingTo("0.40");

    ManagementFeeResult managementFee = resultOf(body, CalculationMetric.MANAGEMENT_FEE, ManagementFeeResult.class);
    assertThat(managementFee.getManagementFee())
        .as("both aggregation modes coincide here, every holding in this portfolio bearing a fee")
        .containsOnlyKeys(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO)
        .allSatisfy((mode, fee) -> assertThat(fee).isEqualByComparingTo("0.014"));
    assertThat(body.path("results").path(CalculationMetric.STANDARD_DEVIATION.getValue()).path("standardDeviation"))
        .as("the deviation was calculated at all, which it could not have been without the shared currency")
        .isNotEmpty();
  }

  /**
   * A metric whose data is missing fails on its own terms — here the ETF carries no management fee, which that metric
   * rejects — and the endpoint reports it under {@code failures} with its notifications while the metrics that could be
   * calculated still return theirs. The status stays 200: the request as a whole succeeded, and it is the per-metric
   * entry that carries the bad news.
   *
   * <p>
   * Only the ETF is missing its fee, deliberately: with both holdings short of one, which holding the failure names
   * would depend on the order the fee resolver happens to walk them in, and the assertion would be about that order
   * rather than about the notification reaching the caller.
   */
  @Test
  void shouldIsolateTheFailure_whenOneMetricCannotBeCalculated() {
    micMockServer.setDispatcher(attributesDispatcher(allAttributesBody(
        managementFeeRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, "1.00", Currency.CAD),
        currencyOnlyRow(WORLD_ETF, FiIdentifierType.TICKER, Currency.CAD))));

    var response = post(writeJson(CompositeCalculationRequest.builder()
        .holdings(sharedHoldings())
        .dataProviders(morningstarOnly())
        .currency(Currency.CAD)
        .commands(List.of(
            metricOnly(CalculationMetric.GEOGRAPHIC_EXPOSURE),
            metricOnly(CalculationMetric.MANAGEMENT_FEE)))
        .build()));

    assertThat(response.status()).isEqualTo(HttpStatus.OK.value());
    JsonNode body = parseJson(response.responseBody());
    assertThat(body.path("results").propertyNames())
        .containsExactly(CalculationMetric.GEOGRAPHIC_EXPOSURE.getValue());
    assertThat(body.path("failures").propertyNames())
        .containsExactly(CalculationMetric.MANAGEMENT_FEE.getValue());

    List<Notification> feeFailure = notificationsOf(body, CalculationMetric.MANAGEMENT_FEE);
    assertThat(feeFailure).hasSize(1);
    String holdingId = sharedHoldings().getLast().getIdsString();
    assertThat(feeFailure.getFirst().getCode()).isEqualTo(ErrorCode.Codes.MISSING_MANAGEMENT_FEE);
    assertThat(feeFailure.getFirst().getMessage())
        .isEqualTo(ErrorCode.MISSING_MANAGEMENT_FEE.getFormattedMessage(holdingId));
    assertThat(feeFailure.getFirst().getMetadata())
        .as("the failure has to name the holding that caused it, which is why only one of the two lacks a fee")
        .containsEntry("holdingId", holdingId);

    GeographicExposureResult exposure = resultOf(body, CalculationMetric.GEOGRAPHIC_EXPOSURE,
        GeographicExposureResult.class);
    assertThat(exposure.getGeographicExposure().get(GeographicRegionType.US)).isEqualByComparingTo("0.60");
  }

  /**
   * Each metric may appear once: a second command for the same metric would have exactly one of the two answers survive
   * the result map, silently, so the request is rejected instead — before any Market Investment Catalogue call is made,
   * which is asserted because a request this malformed must not cost a fetch.
   */
  @Test
  void shouldRejectTheRequest_whenTheSameMetricAppearsTwice() {
    micMockServer.setDispatcher(new QueueDispatcher());
    int requestsBefore = micMockServer.getRequestCount();

    var response = post(writeJson(CompositeCalculationRequest.builder()
        .holdings(sharedHoldings())
        .dataProviders(morningstarOnly())
        .currency(Currency.CAD)
        .commands(List.of(
            metricOnly(CalculationMetric.GEOGRAPHIC_EXPOSURE),
            metricOnly(CalculationMetric.GEOGRAPHIC_EXPOSURE)))
        .build()));

    assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    String metricName = CalculationMetric.GEOGRAPHIC_EXPOSURE.getValue();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.Codes.DUPLICATE_METRIC);
    assertThat(notification.getMessage()).isEqualTo(ErrorCode.DUPLICATE_METRIC.getFormattedMessage(metricName));
    assertThat(notification.getDescription()).isEqualTo(ErrorCode.DUPLICATE_METRIC.getDescription());
    assertThat(notification.getAction()).isEqualTo(ErrorCode.DUPLICATE_METRIC.getAction());
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
    assertThat(notification.getMetadata()).containsEntry("param-1", metricName);
    assertThat(micMockServer.getRequestCount() - requestsBefore)
        .as("a request rejected at the boundary must not reach Market Investment Catalogue")
        .isZero();
  }

  /**
   * The metric is the discriminator that decides which command shape was sent, so a command without one is not a
   * command at all — it deserializes to nothing and is rejected by name rather than as a generic malformed body, which
   * is what tells the caller what to fix. Written as a raw structure rather than a DTO on purpose: no DTO can express
   * this request.
   */
  @Test
  void shouldRejectTheRequest_whenACommandCarriesNoMetric() {
    micMockServer.setDispatcher(new QueueDispatcher());

    var response = post(writeJson(Map.of(
        "holdings", sharedHoldings(),
        "dataProviders", morningstarOnly(),
        "currency", Currency.CAD,
        "commands", List.of(Map.of("timeIntervalPeriods", List.of(TimePeriod.ONE_YR))))));

    assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.Codes.METRIC_REQUIRED);
    assertThat(notification.getMessage()).isEqualTo(ErrorCode.METRIC_REQUIRED.getMessage());
    assertThat(notification.getDescription()).isEqualTo(ErrorCode.METRIC_REQUIRED.getDescription());
    assertThat(notification.getAction()).isEqualTo(ErrorCode.METRIC_REQUIRED.getAction());
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
  }

  /**
   * Every attribute the three metrics need, in the one response Market Investment Catalogue serves for a
   * multi-attribute request: the whole-security regions and the geography rows for the exposure metric, the monthly
   * returns for the deviation, and the fee rows the caller varies per scenario.
   */
  private static String allAttributesBody(MicFeeResponses.FeeRow... feeRows) {
    return writeJson(Map.of(
        CompositeSecurityAttribute.GEOGRAPHIC_ALLOCATION, List.of(
            geographicAllocationRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.US, "1.00")),
            geographicAllocationRow(WORLD_ETF, FiIdentifierType.TICKER, Currency.CAD,
                regionValue(GeographicRegionType.CANADA, "1.00"))),
        CompositeSecurityAttribute.GEOGRAPHY, List.of(
            geographyRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID, null, null, Currency.CAD),
            geographyRow(WORLD_ETF, FiIdentifierType.TICKER, null, null, Currency.CAD)),
        CompositeSecurityAttribute.MONTHLY_RETURNS, List.of(
            returnsRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID),
            returnsRow(WORLD_ETF, FiIdentifierType.TICKER)),
        CompositeSecurityAttribute.FEES, List.of(feeRows)));
  }

  /**
   * Reads one metric's result out of the composite payload. The response type declares its results as the abstract base
   * result, so it deserializes only as a tree — which is also the honest way to assert this endpoint: the keys a client
   * reads are the metric slugs, not enum names.
   */
  private static <T> T resultOf(JsonNode body, CalculationMetric metric, Class<T> type) {
    return readJson(body.path("results").path(metric.getValue()).toString(), type);
  }

  private static List<Notification> notificationsOf(JsonNode body, CalculationMetric metric) {
    return List.of(readJson(body.path("failures").path(metric.getValue()).toString(), Notification[].class));
  }

  private static List<PortfolioHolding> sharedHoldings() {
    return List.of(fund(CANADIAN_FUND, 60_000), etf(WORLD_ETF, 40_000));
  }

  /**
   * A command carrying nothing but its metric — which is the point: everything else it needs is declared once at the
   * top of the request.
   */
  private static CalculationCommand metricOnly(CalculationMetric metric) {
    return PortfolioHoldingsCommand.builder().metric(metric).build();
  }

  private static CalculationCommand standardDeviationWithoutCurrency() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.STANDARD_DEVIATION);
    command.setPeriods(Set.of(TimePeriod.ONE_YR));
    return command;
  }

  private static SecurityAttributeResult<MonthlyReturns> returnsRow(String id, FiIdentifierType idType) {
    List<DateBigDecimalValue> returns = new ArrayList<>();
    LocalDate month = LocalDate.of(2023, 1, 31);
    for (int index = 0; index < MONTHS_OF_HISTORY; index++) {
      LocalDate monthEnd = month.plusMonths(index);
      returns.add(new DateBigDecimalValue(monthEnd.withDayOfMonth(monthEnd.lengthOfMonth()).toString(),
          BigDecimal.valueOf((index % 5) - 2L).movePointLeft(1)));
    }
    MonthlyReturns monthlyReturns = new MonthlyReturns();
    monthlyReturns.setReturns(returns);
    monthlyReturns.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return attributeResult(id, idType, monthlyReturns);
  }

  private Response post(String body) {
    var exchangeResult = webTestClient.post()
        .uri(COMPOSITE_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectBody(String.class)
        .returnResult();
    return new Response(exchangeResult.getStatus().value(), exchangeResult.getResponseBody());
  }

  private record Response(int status, String responseBody) {
  }
}
