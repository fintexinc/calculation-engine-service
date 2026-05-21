package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.rates.DateRateValue;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.RecordedRequest;

@TestPropertySource(properties = {
    "cache.data.fx-rates.enabled=false",
    "cache.data.t-bills.enabled=false"
})
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

  @DynamicPropertySource
  static void registerBocBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.bank-of-canada.base-url",
        () -> bocMockServer.url("/").toString().replaceAll("/$", ""));
  }

  @BeforeEach
  void resetSmsMockServerQueue() {
    smsMockServer.setDispatcher(new QueueDispatcher());
  }

  @Override
  protected String metricPath() {
    return CalculationMetric.TRAILING_TOTAL_RETURNS.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(periodCommand(Set.of("12"), LocalDate.parse("2024-12-31"),
        List.of(
            holding(XBAL, FinancialInstrumentType.ETF_CANADA, "45234.67"),
            holding(VCNS, FinancialInstrumentType.ETF_CANADA, "18765.43"))));
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(periodCommand(Set.of("1", "3", "6", "12"), LocalDate.parse("2024-12-31"),
        richPortfolioHoldings()));
  }

  @Override
  protected void enqueueForPositiveSmsScenario() {
    enqueueSmsMockResponse(smsPositiveResponseBody());
    enqueueSmsMockResponse(writeJson(cadTreasuryRatesSeriesFor2024()));
  }

  @Override
  protected String smsPositiveResponseBody() {
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
    return writeJson(periodCommand(CalculationMetric.SHARPE_RATIO, Set.of("12"), null,
        List.of(holding(XBAL, FinancialInstrumentType.ETF_CANADA, "50000"))));
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    TrailingTotalReturnsResult result = readJson(responseBody, TrailingTotalReturnsResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2024, 1, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));

    assertThat(findPeriod(result, "1").value())
        .isCloseTo(new BigDecimal("0.0116198704"), within(TOLERANCE));
    assertThat(findPeriod(result, "3").value())
        .isCloseTo(new BigDecimal("0.0352662444"), within(TOLERANCE));
    assertThat(findPeriod(result, "6").value())
        .isCloseTo(new BigDecimal("0.0717761968"), within(TOLERANCE));
    assertThat(findPeriod(result, "12").value())
        .isCloseTo(new BigDecimal("0.1487042159"), within(TOLERANCE));
  }

  @Test
  void shouldReturnBadRequest_whenHoldingsListIsEmpty() {
    PeriodCommand command = periodCommand(Set.of("12"), null, List.of());

    assertValidationError(postCalculation(writeJson(command)), "VAL-003", "holdings");
  }

  @Test
  void shouldReturnTrailingReturn_whenSingleMonthReturnIsFivePercent() {
    enqueueSmsMockResponse(writeJson(List.of(
        holdingReturnsRow(XBAL, List.of(dateValue("2024-12-31", "5.0"))))));
    enqueueSmsMockResponse(writeJson(List.of(new DateRateValue(LocalDate.parse("2024-12-31"), new BigDecimal(
        "0.0035")))));

    PeriodCommand command = periodCommand(Set.of("1"), LocalDate.parse("2024-12-31"),
        List.of(holding(XBAL, FinancialInstrumentType.ETF_CANADA, "50000")));
    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    TrailingTotalReturnsResult result = readJson(response.responseBody(), TrailingTotalReturnsResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(findPeriod(result, "1").value())
        .isCloseTo(new BigDecimal("0.05"), within(TOLERANCE));
  }

  private void assertValidationError(HttpResponse response, String expectedCode, String expectedFieldName) {
    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification first = error.getNotifications().getFirst();
    assertThat(first.getCode()).isEqualTo(expectedCode);
    assertThat(first.getSeverity().name()).isEqualTo("ERROR");
    if (expectedFieldName != null) {
      assertThat(first.getFieldName()).isEqualTo(expectedFieldName);
    }
  }

  private static List<PortfolioHolding> richPortfolioHoldings() {
    return List.of(
        holding(XBAL, FinancialInstrumentType.ETF_CANADA, "45234.67"),
        holding(VCNS, FinancialInstrumentType.ETF_CANADA, "33100.50"),
        holding(SPY, FinancialInstrumentType.ETF_US, "25500.00"),
        holding(VTI, FinancialInstrumentType.ETF_US, "10875.25"),
        equity("AAPL", "NASDAQ", FinancialInstrumentType.STOCK_US, "40000.00"),
        equity("RY.TO", "TSX", FinancialInstrumentType.STOCK_CANADA, "28750.00"),
        holding(F0CAN999, FinancialInstrumentType.MUTUAL_FUND_CANADA, "15200.00"),
        holding(CCM4752, FinancialInstrumentType.MUTUAL_FUND_CANADA, "12500.00"),
        holding(VANGUARD_ISIN, FinancialInstrumentType.MUTUAL_FUND_CANADA, "9800.00"));
  }

  private static TimeIntervalResult findPeriod(TrailingTotalReturnsResult result, String period) {
    return result.getTrailingTotalReturn().stream()
        .filter(entry -> period.equals(entry.period()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing period " + period));
  }

  private static PeriodCommand periodCommand(Set<String> periods, LocalDate customPed,
      List<PortfolioHolding> holdings) {
    return periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS, periods, customPed, holdings);
  }

  private static PeriodCommand periodCommand(CalculationMetric metric, Set<String> periods, LocalDate customPed,
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

  private static List<DateBigDecimalValue> monthlyReturnsFor2024(String monthlyPercent) {
    return List.of(
        dateValue("2024-01-31", monthlyPercent),
        dateValue("2024-02-29", monthlyPercent),
        dateValue("2024-03-31", monthlyPercent),
        dateValue("2024-04-30", monthlyPercent),
        dateValue("2024-05-31", monthlyPercent),
        dateValue("2024-06-30", monthlyPercent),
        dateValue("2024-07-31", monthlyPercent),
        dateValue("2024-08-31", monthlyPercent),
        dateValue("2024-09-30", monthlyPercent),
        dateValue("2024-10-31", monthlyPercent),
        dateValue("2024-11-30", monthlyPercent),
        dateValue("2024-12-31", monthlyPercent));
  }

  private static DateBigDecimalValue dateValue(String date, String percent) {
    DateBigDecimalValue dv = new DateBigDecimalValue();
    dv.setDate(date);
    dv.setValue(new BigDecimal(percent));
    return dv;
  }

  private static List<DateRateValue> cadTreasuryRatesSeriesFor2024() {
    NavigableMap<LocalDate, BigDecimal> map = new TreeMap<>();
    map.put(LocalDate.parse("2024-01-31"), new BigDecimal("0.0030"));
    map.put(LocalDate.parse("2024-02-29"), new BigDecimal("0.0031"));
    map.put(LocalDate.parse("2024-03-31"), new BigDecimal("0.0032"));
    map.put(LocalDate.parse("2024-04-30"), new BigDecimal("0.0033"));
    map.put(LocalDate.parse("2024-05-31"), new BigDecimal("0.0034"));
    map.put(LocalDate.parse("2024-06-30"), new BigDecimal("0.0035"));
    map.put(LocalDate.parse("2024-07-31"), new BigDecimal("0.0036"));
    map.put(LocalDate.parse("2024-08-31"), new BigDecimal("0.0037"));
    map.put(LocalDate.parse("2024-09-30"), new BigDecimal("0.0038"));
    map.put(LocalDate.parse("2024-10-31"), new BigDecimal("0.0039"));
    map.put(LocalDate.parse("2024-11-30"), new BigDecimal("0.0040"));
    map.put(LocalDate.parse("2024-12-31"), new BigDecimal("0.0041"));
    return map.entrySet().stream()
        .map(entry -> new DateRateValue(entry.getKey(), entry.getValue()))
        .toList();
  }

  private static PortfolioHolding holding(SecurityIdentifier securityIdentifier, FinancialInstrumentType type,
      String value) {
    return new PortfolioHolding(new BigDecimal(value), type, securityIdentifier);
  }

  private static PortfolioHolding equity(String ticker, String exchange, FinancialInstrumentType type, String value) {
    return new PortfolioHolding(new BigDecimal(value), type, equityId(ticker, exchange));
  }

  private static Dispatcher bocDailyUsdCadDispatcher(String rate) {
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath();
        LocalDate start = queryDate(path, "start_date", LocalDate.now().minusDays(7));
        LocalDate end = queryDate(path, "end_date", LocalDate.now());
        StringBuilder observations = new StringBuilder();
        LocalDate current = start;
        while (!current.isAfter(end)) {
          if (observations.length() > 0) {
            observations.append(',');
          }
          observations.append("{\"d\":\"")
              .append(current)
              .append("\",\"FXUSDCAD\":{\"v\":\"")
              .append(rate)
              .append("\"}}");
          current = current.plusDays(1);
        }
        String body = "{\"observations\":[" + observations + "]}";
        return new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(body);
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
