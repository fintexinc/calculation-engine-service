package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.ErrorParams;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.rates.DateRateValue;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SIX_MTH;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_MTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@TestPropertySource(properties = {
    "cache.data.fx-rates.enabled=false",
    "cache.data.t-bills.enabled=false"
})
@Tag("e2e")
class TrailingTotalReturnsE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final SecurityIdentifier XBAL = new SecurityIdentifier("XBAL", FiIdentifierType.TICKER);
  private static final SecurityIdentifier VCNS = new SecurityIdentifier("VCNS", FiIdentifierType.TICKER);
  private static final SecurityIdentifier SPY = new SecurityIdentifier("SPY", FiIdentifierType.TICKER);
  private static final SecurityIdentifier VTI = new SecurityIdentifier("VTI", FiIdentifierType.TICKER);
  private static final SecurityIdentifier F0CAN999 = new SecurityIdentifier("F0CAN999",
      FiIdentifierType.MORNINGSTAR_ID);
  private static final SecurityIdentifier CCM4752 = new SecurityIdentifier("CCM4752", FiIdentifierType.FUNDSERV);
  private static final SecurityIdentifier VANGUARD_ISIN = new SecurityIdentifier("CA92203F1062",
      FiIdentifierType.ISIN);

  private static final BigDecimal TOLERANCE = new BigDecimal("0.000001");
  private static MockWebServer bocMockServer;

  @BeforeAll
  static void startBocMockServer() throws IOException {
    bocMockServer = new MockWebServer();
    bocMockServer.setDispatcher(bocDailyUsdCadDispatcher("1.0000"));
    bocMockServer.start();
  }

  @AfterAll
  static void shutdownBocMockServer() throws IOException {
    if (bocMockServer != null) {
      bocMockServer.shutdown();
      bocMockServer = null;
    }
  }

  @BeforeEach
  void resetBocMockServer() {
    bocMockServer.setDispatcher(bocDailyUsdCadDispatcher("1.0000"));
  }

  @DynamicPropertySource
  static void registerBocBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.bank-of-canada.base-url",
        () -> bocMockServer.url("/").toString().replaceAll("/$", ""));
  }

  @Override
  protected String metricPath() {
    return CalculationMetric.TRAILING_TOTAL_RETURNS.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(periodCommand(Set.of(ONE_YR), LocalDate.parse("2024-12-31"), richPortfolioHoldings()));
  }

  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(periodCommand(Set.of(ONE_MTH, THREE_MTH, SIX_MTH, ONE_YR), LocalDate.parse("2024-12-31"),
        richPortfolioHoldings()));
  }

  @Override
  protected void enqueueForPositiveMicScenario() {
    enqueueMicMockResponse(micPositiveResponseBody());
    enqueueMicMockResponse(writeJson(cadTreasuryRatesSeriesFor2024()));
  }

  @Override
  protected String micPositiveResponseBody() {
    return writeJson(List.of(
        holdingReturnsRow(XBAL, monthlyReturnsFor2024("1.0")),
        holdingReturnsRow(VCNS, monthlyReturnsFor2024("1.2")),
        holdingReturnsRow(SPY, monthlyReturnsFor2024("0.8")),
        holdingReturnsRow(VTI, monthlyReturnsFor2024("1.5")),
        holdingReturnsRow(equityId("AAPL", "NASDAQ"), monthlyReturnsFor2024("2.0")),
        holdingReturnsRow(equityId("RY.TO", "TSX"), monthlyReturnsFor2024("0.5")),
        holdingReturnsRow(F0CAN999, monthlyReturnsFor2024("1.1")),
        holdingReturnsRow(CCM4752, monthlyReturnsFor2024("0.9")),
        holdingReturnsRow(VANGUARD_ISIN, monthlyReturnsFor2024("1.3"))));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    return writeJson(periodCommand(CalculationMetric.SHARPE_RATIO, Set.of(ONE_YR), null,
        richPortfolioHoldings()));
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    TrailingTotalReturnsResult result = readJson(responseBody, TrailingTotalReturnsResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2024, 1, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    assertThat(result.getTrailingTotalReturn()).hasSize(4);

    assertThat(findPeriod(result, ONE_MTH.name()).value())
        .isCloseTo(new BigDecimal("0.0176198704"), within(TOLERANCE));
    assertThat(findPeriod(result, THREE_MTH.name()).value())
        .isCloseTo(new BigDecimal("0.0475271907"), within(TOLERANCE));
    assertThat(findPeriod(result, SIX_MTH.name()).value())
        .isCloseTo(new BigDecimal("0.0832827785"), within(TOLERANCE));
    assertThat(findPeriod(result, ONE_YR.name()).value())
        .isCloseTo(new BigDecimal("0.1503798415"), within(TOLERANCE));
  }

  @Test
  void shouldReturnComparison_whenBenchmarkHoldingsAreProvided() {
    enqueueMicMockResponse(writeJson(List.of(
        holdingReturnsRow(XBAL, List.of(dateValue("2024-12-31", "5.0"))))));
    enqueueMicMockResponse(writeJson(List.of(
        holdingReturnsRow(VCNS, List.of(dateValue("2024-12-31", "2.5"))))));
    enqueueMicMockResponse(writeJson(List.of(new DateRateValue(LocalDate.parse("2024-12-31"), new BigDecimal(
        "0.0035")))));
    PeriodCommand command = periodCommand(Set.of(ONE_MTH), LocalDate.parse("2024-12-31"),
        List.of(holding(XBAL, FinancialInstrumentType.ETF, Country.CANADA, "50000")));
    command.setBenchmarkHoldings(List.of(holding(VCNS, FinancialInstrumentType.ETF, Country.CANADA, "50000")));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    TrailingTotalReturnsResult result = readJson(response.responseBody(), TrailingTotalReturnsResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getTrailingTotalReturn()).singleElement().satisfies(portfolio -> {
      assertThat(portfolio.period()).isEqualTo(ONE_MTH.name());
      assertThat(portfolio.value()).isCloseTo(new BigDecimal("0.05"), within(TOLERANCE));
    });
    assertThat(result.getComparison()).singleElement().satisfies(comparison -> {
      assertThat(comparison.period()).isEqualTo(ONE_MTH);
      assertThat(comparison.portfolio()).isCloseTo(new BigDecimal("0.05"), within(TOLERANCE));
      assertThat(comparison.benchmark()).isCloseTo(new BigDecimal("0.025"), within(TOLERANCE));
      assertThat(comparison.percentDifference()).isCloseTo(new BigDecimal("100"), within(TOLERANCE));
    });
  }

  @Test
  void shouldPreservePortfolioHistory_whenBenchmarkHistoryIsShorter() {
    enqueueMicMockResponse(writeJson(List.of(
        holdingReturnsRow(XBAL, List.of(
            dateValue("2024-10-31", "1.0"),
            dateValue("2024-11-30", "2.0"),
            dateValue("2024-12-31", "3.0"))))));
    enqueueMicMockResponse(writeJson(List.of(
        holdingReturnsRow(VCNS, List.of(dateValue("2024-12-31", "2.5"))))));
    enqueueMicMockResponse(writeJson(List.of(
        new DateRateValue(LocalDate.parse("2024-10-31"), new BigDecimal("0.0033")),
        new DateRateValue(LocalDate.parse("2024-11-30"), new BigDecimal("0.0034")),
        new DateRateValue(LocalDate.parse("2024-12-31"), new BigDecimal("0.0035")))));
    PeriodCommand command = periodCommand(Set.of(THREE_MTH), LocalDate.parse("2024-12-31"),
        List.of(holding(XBAL, FinancialInstrumentType.ETF, Country.CANADA, "50000")));
    command.setBenchmarkHoldings(List.of(holding(VCNS, FinancialInstrumentType.ETF, Country.CANADA, "50000")));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    TrailingTotalReturnsResult result = readJson(response.responseBody(), TrailingTotalReturnsResult.class);
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2024, 10, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    assertThat(findPeriod(result, THREE_MTH.name()).value())
        .isCloseTo(new BigDecimal("0.061106"), within(TOLERANCE));
    assertThat(result.getWarnings()).extracting(Notification::getCode)
        .containsExactly(ErrorCode.Codes.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD);
    assertThat(result.getComparison()).singleElement().satisfies(comparison -> {
      assertThat(comparison.period()).isEqualTo(THREE_MTH);
      assertThat(comparison.portfolio()).isCloseTo(new BigDecimal("0.061106"), within(TOLERANCE));
      assertThat(comparison.benchmark()).isNull();
      assertThat(comparison.percentDifference()).isNull();
    });
  }

  @Test
  void shouldPreservePortfolioReturn_whenBenchmarkReturnsAreUnavailable() {
    enqueueMicMockResponse(writeJson(List.of(
        holdingReturnsRow(XBAL, List.of(dateValue("2024-12-31", "5.0"))))));
    enqueueMicMockResponse(writeJson(List.of()));
    enqueueMicMockResponse(writeJson(List.of(new DateRateValue(LocalDate.parse("2024-12-31"), new BigDecimal(
        "0.0035")))));
    PeriodCommand command = periodCommand(Set.of(ONE_MTH), LocalDate.parse("2024-12-31"),
        List.of(holding(XBAL, FinancialInstrumentType.ETF, Country.CANADA, "50000")));
    PortfolioHolding benchmarkHolding = holding(VCNS, FinancialInstrumentType.ETF, Country.CANADA, "50000");
    command.setBenchmarkHoldings(List.of(benchmarkHolding));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    TrailingTotalReturnsResult result = readJson(response.responseBody(), TrailingTotalReturnsResult.class);
    assertThat(result.getTrailingTotalReturn()).singleElement().satisfies(portfolio -> {
      assertThat(portfolio.period()).isEqualTo(ONE_MTH.name());
      assertThat(portfolio.value()).isCloseTo(new BigDecimal("0.05"), within(TOLERANCE));
    });
    assertThat(result.getComparison()).singleElement().satisfies(comparison -> {
      assertThat(comparison.period()).isEqualTo(ONE_MTH);
      assertThat(comparison.portfolio()).isCloseTo(new BigDecimal("0.05"), within(TOLERANCE));
      assertThat(comparison.benchmark()).isNull();
      assertThat(comparison.percentDifference()).isNull();
    });
    assertThat(result.getWarnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.NO_SECURITY_DATA_FOR_HOLDING);
      assertThat(warning.getMessage()).isEqualTo("No data returned for holding " + benchmarkHolding.getIdsString());
      assertThat(warning.getMetadata()).containsEntry("holdingId", benchmarkHolding.getIdsString());
    });
  }

  @Test
  void shouldReturnOnlyContiguousPeriodsAndFxWarning_whenFxRatesHaveInternalMonthGap() {
    bocMockServer.setDispatcher(bocDailyUsdCadDispatcher("1.0000", Set.of(YearMonth.of(2024, 6))));
    PortfolioHolding holding = holding(VTI, FinancialInstrumentType.ETF, Country.USA, "50000");
    enqueueMicMockResponse(writeJson(List.of(holdingReturnsRow(VTI, monthlyReturnsFor2024("1.0")))));
    enqueueMicMockResponse(writeJson(cadTreasuryRatesSeriesFor2024()));

    HttpResponse response = postCalculation(writeJson(
        periodCommand(Set.of(THREE_MTH, SIX_MTH), LocalDate.parse("2024-12-31"), List.of(holding))));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    TrailingTotalReturnsResult result = readJson(response.responseBody(), TrailingTotalReturnsResult.class);
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2024, 8, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    assertThat(result.getWarnings()).extracting(Notification::getCode)
        .containsExactlyInAnyOrder(
            ErrorCode.Codes.FX_RATES_UNAVAILABLE,
            ErrorCode.Codes.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD);
    Notification warning = result.getWarnings().stream()
        .filter(notification -> ErrorCode.Codes.FX_RATES_UNAVAILABLE.equals(notification.getCode()))
        .findFirst()
        .orElseThrow();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.FX_RATES_UNAVAILABLE);
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMessage()).isEqualTo(
        "FX rates unavailable for holding " + holding.getIdsString() + ": USD -> CAD");
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holding.getIdsString())
        .containsEntry("param-1", holding.getIdsString())
        .containsEntry("param-2", Currency.USD.name())
        .containsEntry("param-3", Currency.CAD.name());
    assertThat(result.getTrailingTotalReturn()).hasSize(2);
    assertThat(findPeriod(result, THREE_MTH.name()).value())
        .isCloseTo(new BigDecimal("0.04252268"), within(TOLERANCE));
    assertThat(findPeriod(result, SIX_MTH.name()).value()).isNull();
  }

  @Test
  void shouldReturnFxRatesUnavailableBusinessError_whenAllRequiredFxRatesAreUnavailable() {
    bocMockServer.setDispatcher(bocUnavailableDispatcher());
    PortfolioHolding holding = holding(VTI, FinancialInstrumentType.ETF, Country.USA, "50000");
    enqueueMicMockResponse(writeJson(List.of(holdingReturnsRow(VTI, monthlyReturnsFor2024("1.0")))));

    HttpResponse response = postCalculation(writeJson(
        periodCommand(Set.of(ONE_YR), LocalDate.parse("2024-12-31"), List.of(holding))));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.Codes.FX_RATES_UNAVAILABLE);
    assertThat(notification.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(notification.getMessage()).isEqualTo("FX rates unavailable for holding " + holding.getIdsString()
        + ": USD -> CAD");
    assertThat(notification.getMetadata())
        .containsEntry("holdingId", holding.getIdsString())
        .containsEntry("param-1", holding.getIdsString())
        .containsEntry("param-2", Currency.USD.name())
        .containsEntry("param-3", Currency.CAD.name());
  }

  @Test
  void shouldReturnBadRequestWithMissingMonthlyReturnError_whenMonthlyReturnValueIsNull() {
    enqueueMicMockResponse(writeJson(List.of(
        holdingReturnsRow(F0CAN999, monthlyReturnsWithNullValue("2024-05-01")))));
    PeriodCommand command = periodCommand(Set.of(ONE_YR), LocalDate.parse("2024-12-31"),
        List.of(holding(F0CAN999, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100000")));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.MISSING_MONTHLY_RETURN_FOR_DATE.getCode());
    assertThat(notification.getMessage())
        .isEqualTo("The holding is missing monthly return values for date 2024-05-31");
    assertThat(notification.getDescription()).isEqualTo("Monthly return is missing for the specified date");
    assertThat(notification.getAction()).isEqualTo("Populate the monthly return for the missing date");
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
    assertThat(notification.getMetadata())
        .containsOnlyKeys(ErrorParams.HOLDING_ID, "param-1")
        .containsEntry(ErrorParams.HOLDING_ID, "MUTUAL_FUND-F0CAN999")
        .containsEntry("param-1", "2024-05-31");
  }

  private Notification assertValidationError(HttpResponse response, String expectedCode, String expectedFieldName) {
    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification first = error.getNotifications().getFirst();
    assertThat(first.getCode()).isEqualTo(expectedCode);
    assertThat(first.getSeverity()).isEqualTo(Severity.ERROR);
    if (expectedFieldName != null) {
      assertThat(first.getFieldName()).isEqualTo(expectedFieldName);
    }
    return first;
  }

  private static List<PortfolioHolding> richPortfolioHoldings() {
    return List.of(
        holding(XBAL, FinancialInstrumentType.ETF, Country.CANADA, "45234.67"),
        holding(VCNS, FinancialInstrumentType.ETF, Country.CANADA, "33100.50"),
        holding(SPY, FinancialInstrumentType.ETF, Country.USA, "25500.00"),
        holding(VTI, FinancialInstrumentType.ETF, Country.USA, "10875.25"),
        equity("AAPL", "NASDAQ", FinancialInstrumentType.STOCK, Country.USA, "40000.00"),
        equity("RY.TO", "TSX", FinancialInstrumentType.STOCK, Country.CANADA, "28750.00"),
        holding(F0CAN999, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "15200.00"),
        holding(CCM4752, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "12500.00"),
        holding(VANGUARD_ISIN, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "9800.00"));
  }

  private static TimeIntervalResult findPeriod(TrailingTotalReturnsResult result, String period) {
    return result.getTrailingTotalReturn().stream()
        .filter(entry -> period.equals(entry.period()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing period " + period));
  }

  private static PeriodCommand periodCommand(Set<TimePeriod> periods, LocalDate customPed,
      List<PortfolioHolding> holdings) {
    return periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS, periods, customPed, holdings);
  }

  private static PeriodCommand periodCommand(CalculationMetric metric, Set<TimePeriod> periods, LocalDate customPed,
      List<PortfolioHolding> holdings) {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(metric);
    command.setCurrency(Currency.CAD);
    command.setPeriods(periods);
    command.setCustomPed(customPed);
    command.setHoldings(holdings);
    return command;
  }

  private static SecurityAttributeResult<MonthlyReturns> holdingReturnsRow(SecurityIdentifier identifier,
      List<DateBigDecimalValue> returns) {
    MonthlyReturns response = new MonthlyReturns();
    response.setReturns(returns);
    response.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    SecurityAttributeResult<MonthlyReturns> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(response);
    return result;
  }

  private static EquitySecurityIdentifier equityId(String ticker, String exchange) {
    return EquitySecurityIdentifier.builder()
        .id(ticker)
        .idType(FiIdentifierType.TICKER_MIC)
        .exchangeId(exchange)
        .build();
  }

  private static final String[] MONTH_ENDS_2024 = {
      "2024-01-31", "2024-02-29", "2024-03-31", "2024-04-30", "2024-05-31", "2024-06-30",
      "2024-07-31", "2024-08-31", "2024-09-30", "2024-10-31", "2024-11-30", "2024-12-31"};

  // Month-to-month variation (percentage points, incl. down months) layered on each holding's base return,
  // so the input is a realistic 12-month path rather than a flat constant repeated 12 times.
  private static final double[] MONTHLY_DELTAS_2024 = {
      0.0, -0.7, 0.9, -1.6, 1.4, -0.9, 0.3, 0.8, -1.2, 1.1, -0.5, 0.6};

  private static List<DateBigDecimalValue> monthlyReturnsFor2024(String basePercent) {
    BigDecimal base = new BigDecimal(basePercent);
    return IntStream.range(0, MONTH_ENDS_2024.length)
        .mapToObj(i -> dateValue(MONTH_ENDS_2024[i],
            base.add(BigDecimal.valueOf(MONTHLY_DELTAS_2024[i])).toPlainString()))
        .toList();
  }

  private static List<DateBigDecimalValue> monthlyReturnsWithNullValue(String date) {
    YearMonth affectedMonth = YearMonth.from(LocalDate.parse(date));
    return monthlyReturnsFor2024("1.0").stream()
        .map(monthlyReturn -> affectedMonth.equals(YearMonth.from(LocalDate.parse(monthlyReturn.getDate())))
            ? dateValueWithNullValue(date)
            : monthlyReturn)
        .toList();
  }

  private static DateBigDecimalValue dateValue(String date, String percent) {
    DateBigDecimalValue dv = new DateBigDecimalValue();
    dv.setDate(date);
    dv.setValue(new BigDecimal(percent));
    return dv;
  }

  private static DateBigDecimalValue dateValueWithNullValue(String date) {
    DateBigDecimalValue value = new DateBigDecimalValue();
    value.setDate(date);
    value.setValue(null);
    return value;
  }

  private static List<DateRateValue> cadTreasuryRatesSeriesFor2024() {
    // Month i gets rate 0.00(30+i): 0.0030, 0.0031, ... 0.0041.
    return IntStream.range(0, MONTH_ENDS_2024.length)
        .mapToObj(i -> new DateRateValue(LocalDate.parse(MONTH_ENDS_2024[i]), BigDecimal.valueOf(30L + i, 4)))
        .toList();
  }

  private static PortfolioHolding holding(SecurityIdentifier securityIdentifier, FinancialInstrumentType type,
      Country country, String value) {
    return new PortfolioHolding(new BigDecimal(value), type, country, securityIdentifier);
  }

  private static PortfolioHolding equity(String ticker, String exchange, FinancialInstrumentType type,
      Country country, String value) {
    return new PortfolioHolding(new BigDecimal(value), type, country, equityId(ticker, exchange));
  }

  private static Dispatcher bocDailyUsdCadDispatcher(String rate) {
    return bocDailyUsdCadDispatcher(rate, Set.of());
  }

  private static Dispatcher bocDailyUsdCadDispatcher(String rate, Set<YearMonth> unavailableMonths) {
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath();
        LocalDate start = queryDate(path, "start_date", LocalDate.now().minusDays(7));
        LocalDate end = queryDate(path, "end_date", LocalDate.now());
        StringBuilder observations = new StringBuilder();
        LocalDate current = start;
        while (!current.isAfter(end)) {
          if (!unavailableMonths.contains(YearMonth.from(current))) {
            if (observations.length() > 0) {
              observations.append(',');
            }
            observations.append("{\"d\":\"")
                .append(current)
                .append("\",\"FXUSDCAD\":{\"v\":\"")
                .append(rate)
                .append("\"}}");
          }
          current = current.plusDays(1);
        }
        String body = "{\"observations\":[" + observations + "]}";
        return new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(body);
      }
    };
  }

  private static Dispatcher bocUnavailableDispatcher() {
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        return new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"observations\":[]}");
      }
    };
  }

  private static LocalDate queryDate(String path, String param, LocalDate fallback) {
    if (path == null) {
      return fallback;
    }
    String token = param + "=";
    int start = path.indexOf(token);
    if (start < 0) {
      return fallback;
    }
    int valueStart = start + token.length();
    int valueEnd = path.indexOf('&', valueStart);
    String value = valueEnd < 0 ? path.substring(valueStart) : path.substring(valueStart, valueEnd);
    return value.isBlank() ? fallback : LocalDate.parse(value);
  }
}
