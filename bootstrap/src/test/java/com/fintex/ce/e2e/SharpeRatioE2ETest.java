package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
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

import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import okhttp3.mockwebserver.MockWebServer;

/**
 * End-to-end HTTP-boundary coverage for the {@code sharpe-ratio} metric's T-Bill contract: a complete risk-free series
 * across the requested 12-month window produces a Sharpe ratio, while a series with a gap inside the window is rejected
 * with {@code TBL-001} ({@code MISSING_TBILL_RATE}) rather than silently producing a wrong or null result.
 */
@TestPropertySource(properties = {
    "cache.data.fx-rates.enabled=false",
    "cache.data.t-bills.enabled=false"
})
class SharpeRatioE2ETest extends AbstractPortfolioCalculationE2ETest {

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
    bocMockServer.setDispatcher(BocMockResponses.dailyUsdCadDispatcher());
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

  @Override
  protected String metricPath() {
    return CalculationMetric.SHARPE_RATIO.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(periodCommand(Set.of(ONE_YR), LocalDate.parse("2024-12-31"), richPortfolioHoldings()));
  }

  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(periodCommand(Set.of(ONE_YR), LocalDate.parse("2024-12-31"), richPortfolioHoldings()));
  }

  @Override
  protected void enqueueForPositiveMicScenario() {
    // The orchestrator fetches the portfolio monthly returns first, then SharpeRatioCalculationServiceImpl fetches the
    // T-Bill series, so the returns response must be enqueued before the treasury-rates response.
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
    return writeJson(periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS, Set.of(ONE_YR), null,
        richPortfolioHoldings()));
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    SharpeRatioResult result = readJson(responseBody, SharpeRatioResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2024, 1, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    assertThat(result.getSharpeRatio()).hasSize(1);
    assertThat(findPeriod(result, ONE_YR.name()).value())
        .isCloseTo(new BigDecimal("3.0056605434"), within(TOLERANCE));
  }

  @Test
  void shouldReturnBadRequest_whenTBillRateMissingForMonthInsideRequestedWindow() {
    enqueueMicMockResponse(micPositiveResponseBody());
    enqueueMicMockResponse(writeJson(cadTreasuryRatesSeriesFor2024WithGapIn("2024-07-31")));

    PeriodCommand command = periodCommand(Set.of(ONE_YR), LocalDate.parse("2024-12-31"), richPortfolioHoldings());
    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(ErrorCode.Codes.MISSING_TBILL_RATE);
    assertThat(notification.getMessage()).isEqualTo("Missing T-Bill rate for date 2024-07-31");
    assertThat(notification.getMetadata()).hasSize(1).containsEntry("param-1", "2024-07-31");
  }

  private static TimeIntervalResult findPeriod(SharpeRatioResult result, String period) {
    return result.getSharpeRatio().stream()
        .filter(entry -> period.equals(entry.period()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing period " + period));
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

  private static PeriodCommand periodCommand(Set<TimePeriod> periods, LocalDate customPed,
      List<PortfolioHolding> holdings) {
    return periodCommand(CalculationMetric.SHARPE_RATIO, periods, customPed, holdings);
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

  private static EquitySecurityIdentifier equityId(String ticker, String exchange) {
    return EquitySecurityIdentifier.builder()
        .id(ticker)
        .idType(FiIdentifierType.TICKER_MIC)
        .exchangeId(exchange)
        .build();
  }

  private static PortfolioHolding equity(String ticker, String exchange, FinancialInstrumentType type,
      Country country, String value) {
    return new PortfolioHolding(new BigDecimal(value), type, country, equityId(ticker, exchange));
  }

  private static DateBigDecimalValue dateValue(String date, String percent) {
    DateBigDecimalValue dv = new DateBigDecimalValue();
    dv.setDate(date);
    dv.setValue(new BigDecimal(percent));
    return dv;
  }

  private static List<DateRateValue> cadTreasuryRatesSeriesFor2024() {
    return cadTreasuryRatesSeriesFor2024WithGapIn(null);
  }

  private static List<DateRateValue> cadTreasuryRatesSeriesFor2024WithGapIn(String dateToOmit) {
    // Month i gets rate 0.00(30+i): 0.0030, 0.0031, ... 0.0041; dateToOmit (if any) is dropped to simulate a gap.
    return IntStream.range(0, MONTH_ENDS_2024.length)
        .filter(i -> !MONTH_ENDS_2024[i].equals(dateToOmit))
        .mapToObj(i -> new DateRateValue(LocalDate.parse(MONTH_ENDS_2024[i]), BigDecimal.valueOf(30L + i, 4)))
        .toList();
  }

  private static PortfolioHolding holding(SecurityIdentifier securityIdentifier, FinancialInstrumentType type,
      Country country, String value) {
    return new PortfolioHolding(new BigDecimal(value), type, country, securityIdentifier);
  }
}
