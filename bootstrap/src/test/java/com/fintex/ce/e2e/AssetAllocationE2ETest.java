package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.AssetAllocationValue;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.RegionDatapoint;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * End-to-end coverage for the {@code /asset-allocations} endpoint. Beyond the inherited single-ETF positive case, this
 * class exercises the full stack on portfolios that mix cash, GIC, stocks, ETFs, and funds across multiple currencies —
 * verifying weighted aggregation after FX normalization to CAD, geography lookups for stocks, asset-allocation lookups
 * for funds/ETFs, and warning emission when SMS or Bank of Canada return incomplete data. Request bodies are built from
 * the same DTO classes the controller deserializes, so renames or shape changes break the test at compile time. The FX
 * cache is disabled here so every test sees the dispatcher state it installs; caching behaviour is covered by
 * {@link FxRatesCachingEnabledE2ETest}.
 */
@Tag("e2e")
@TestPropertySource(properties = "cache.data.fx-rates.enabled=false")
class AssetAllocationE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String GEOGRAPHY_PATH = "/api/v1/wealth/securities/geography";
  private static final String ASSET_ALLOCATION_PATH = "/api/v1/wealth/securities/allocations/asset";
  private static final BigDecimal TOLERANCE = new BigDecimal("0.0001");
  private static final List<DataProvider> MORNINGSTAR_ONLY = List.of(DataProvider.MORNINGSTAR);

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

  @AfterEach
  void resetMockDispatchers() {
    smsMockServer.setDispatcher(new QueueDispatcher());
    bocMockServer.setDispatcher(BocMockResponses.dailyUsdCadDispatcher());
  }

  @DynamicPropertySource
  static void registerBocBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.bank-of-canada.base-url",
        () -> bocMockServer.url("/").toString().replaceAll("/$", ""));
  }

  @Override
  protected String metricPath() {
    return CalculationMetric.ASSET_ALLOCATIONS.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(allocationsCommand(
        etf("XBAL", FinancialInstrumentType.ETF_CANADA, 50_000),
        etf("VCNS", FinancialInstrumentType.ETF_CANADA, 50_000)));
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(allocationsCommand(etf("XBAL", FinancialInstrumentType.ETF_CANADA, 50_000)));
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(List.of(allocationRow("XBAL", FiIdentifierType.TICKER, Currency.CAD,
        allocationValue(AssetAllocationRegionType.US_EQUITIES, "0.6"),
        allocationValue(AssetAllocationRegionType.FIXED_INCOME, "0.3"),
        allocationValue(AssetAllocationRegionType.CASH, "0.1"))));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(etf("XBAL", FinancialInstrumentType.ETF_CANADA, 50_000)));
    return writeJson(command);
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    AssetAllocationResult result = readJson(responseBody, AssetAllocationResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertCloseTo(result, AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.6"));
    assertCloseTo(result, AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("0.3"));
    assertCloseTo(result, AssetAllocationRegionType.CASH, new BigDecimal("0.1"));
  }

  @Test
  void shouldAggregateWeightedAllocations_acrossMixedHoldingsAndCurrencies() {
    bocMockServer.setDispatcher(constantBocRateDispatcher("1.5000"));
    smsMockServer.setDispatcher(routingDispatcher(
        List.of(
            geographyRow("AAPL", FiIdentifierType.TICKER_MIC, SecurityRegion.USA, Currency.USD),
            geographyRow("RY.TO", FiIdentifierType.TICKER_MIC, SecurityRegion.CANADA, Currency.CAD)),
        List.of(
            allocationRow("SPY", FiIdentifierType.TICKER, Currency.USD,
                allocationValue(AssetAllocationRegionType.US_EQUITIES, "1.0")),
            allocationRow("F0CAN999", FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                allocationValue(AssetAllocationRegionType.US_EQUITIES, "0.5"),
                allocationValue(AssetAllocationRegionType.FIXED_INCOME, "0.3"),
                allocationValue(AssetAllocationRegionType.CASH, "0.1"),
                allocationValue(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, "0.1")))));

    var response = postCalculation(writeJson(mixedPortfolioCommand()));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    AssetAllocationResult result = readJson(response.responseBody(), AssetAllocationResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertCloseTo(result, AssetAllocationRegionType.CASH, new BigDecimal("0.14"));
    assertCloseTo(result, AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("0.32"));
    assertCloseTo(result, AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.44"));
    assertCloseTo(result, AssetAllocationRegionType.CANADIAN_EQUITIES, new BigDecimal("0.06"));
    assertCloseTo(result, AssetAllocationRegionType.INTERNATIONAL_EQUITIES, new BigDecimal("0.04"));
  }

  @Test
  void shouldEmitWarnings_whenStockHasNoGeographyAndFundHasNoAllocations() {
    smsMockServer.setDispatcher(routingDispatcher(
        List.of(geographyRow("RY.TO", FiIdentifierType.TICKER_MIC, SecurityRegion.CANADA, Currency.CAD)),
        List.of(allocationRow("F0CAN999", FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
            allocationValue(AssetAllocationRegionType.US_EQUITIES, "1.0")))));

    var response = postCalculation(writeJson(warningsPortfolioCommand()));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    AssetAllocationResult result = readJson(response.responseBody(), AssetAllocationResult.class);
    assertThat(result.getWarnings()).extracting(Notification::getCode)
        .containsExactlyInAnyOrder("FDS-026", "FDS-018");
    assertThat(result.getAssetAllocation().keySet().stream().map(Object::toString).toList())
        .contains("UNCLASSIFIED", "CANADIAN_EQUITIES", "US_EQUITIES");
  }

  @Test
  void shouldEmitFxWarning_whenBankOfCanadaReturnsNoObservations() {
    bocMockServer.setDispatcher(emptyBocObservationsDispatcher());
    smsMockServer.setDispatcher(routingDispatcher(
        List.of(geographyRow("AAPL", FiIdentifierType.TICKER_MIC, SecurityRegion.USA, Currency.USD)),
        List.of()));

    var response = postCalculation(writeJson(singleUsdStockCommand()));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    AssetAllocationResult result = readJson(response.responseBody(), AssetAllocationResult.class);
    assertThat(result.getWarnings()).extracting(Notification::getCode).containsExactly("FX-001");
    assertCloseTo(result, AssetAllocationRegionType.US_EQUITIES, BigDecimal.ONE);
  }

  private static PortfolioHoldingsCommand mixedPortfolioCommand() {
    return allocationsCommand(
        cash("CASH-CAD", Currency.CAD, 50),
        gic("GIC-RBC-3Y", Currency.CAD, 100, 1095),
        equity("AAPL", "NASDAQ", FinancialInstrumentType.STOCK_US, 40),
        etf("SPY", FinancialInstrumentType.ETF_US, 40),
        equity("RY.TO", "TSX", FinancialInstrumentType.STOCK_CANADA, 30),
        fund("F0CAN999", FinancialInstrumentType.MUTUAL_FUND_CANADA, 200));
  }

  private static PortfolioHoldingsCommand warningsPortfolioCommand() {
    return allocationsCommand(
        equity("RY.TO", "TSX", FinancialInstrumentType.STOCK_CANADA, 100),
        equity("GHOST", "NASDAQ", FinancialInstrumentType.STOCK_US, 100),
        fund("F0CAN999", FinancialInstrumentType.MUTUAL_FUND_CANADA, 100),
        fund("F0CAN-EMPTY", FinancialInstrumentType.MUTUAL_FUND_CANADA, 100));
  }

  private static PortfolioHoldingsCommand singleUsdStockCommand() {
    return allocationsCommand(equity("AAPL", "NASDAQ", FinancialInstrumentType.STOCK_US, 1000));
  }

  private static PortfolioHoldingsCommand allocationsCommand(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.ASSET_ALLOCATIONS)
        .holdings(List.of(holdings))
        .dataProviders(MORNINGSTAR_ONLY)
        .build();
  }

  private static CashHolding cash(String id, Currency currency, long value) {
    return CashHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.CASH)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .currency(currency)
        .build();
  }

  private static GicHolding gic(String id, Currency currency, long value, long termDays) {
    return GicHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.GIC)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .currency(currency)
        .term(BigDecimal.valueOf(termDays))
        .build();
  }

  private static PortfolioHolding equity(String ticker, String exchange, FinancialInstrumentType type, long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), type,
        EquitySecurityIdentifier.builder().id(ticker).idType(FiIdentifierType.TICKER_MIC).exchangeId(exchange).build());
  }

  private static PortfolioHolding etf(String ticker, FinancialInstrumentType type, long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), type,
        new SecurityIdentifier(ticker, FiIdentifierType.TICKER));
  }

  private static PortfolioHolding fund(String morningstarId, FinancialInstrumentType type, long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), type,
        new SecurityIdentifier(morningstarId, FiIdentifierType.MORNINGSTAR_ID));
  }

  private static void assertCloseTo(AssetAllocationResult result, AssetAllocationRegionType region,
      BigDecimal expected) {
    BigDecimal actual = result.getAssetAllocation().get(region);
    assertThat(actual).as("region %s", region).isNotNull();
    assertThat(actual).as("region %s", region).isCloseTo(expected, within(TOLERANCE));
  }

  private static Dispatcher routingDispatcher(List<SecurityAttributeResult<Geography>> geographyRows,
      List<SecurityAttributeResult<AssetAllocationWithCurrency>> allocationRows) {
    String geographyBody = writeJson(geographyRows);
    String allocationBody = writeJson(allocationRows);
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath();
        if (path != null && path.contains(GEOGRAPHY_PATH)) {
          return jsonResponse(geographyBody);
        }
        if (path != null && path.contains(ASSET_ALLOCATION_PATH)) {
          return jsonResponse(allocationBody);
        }
        return new MockResponse().setResponseCode(404);
      }
    };
  }

  private static Dispatcher constantBocRateDispatcher(String rate) {
    String body = "{\"observations\":[{\"d\":\"" + LocalDate.now()
        + "\",\"FXUSDCAD\":{\"v\":\"" + rate + "\"}}]}";
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        return jsonResponse(body);
      }
    };
  }

  private static Dispatcher emptyBocObservationsDispatcher() {
    String body = "{\"observations\":[]}";
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        return jsonResponse(body);
      }
    };
  }

  private static MockResponse jsonResponse(String body) {
    return new MockResponse()
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody(body);
  }

  private static SecurityAttributeResult<Geography> geographyRow(String id, FiIdentifierType idType,
      SecurityRegion region, Currency currency) {
    RegionDatapoint regionDp = new RegionDatapoint();
    regionDp.setValue(region);
    CurrencyDatapoint currencyDp = new CurrencyDatapoint();
    currencyDp.setValue(currency);
    Geography geography = new Geography();
    geography.setRegion(regionDp);
    geography.setCurrency(currencyDp);
    geography.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return attributeResult(id, idType, geography);
  }

  private static SecurityAttributeResult<AssetAllocationWithCurrency> allocationRow(String id, FiIdentifierType idType,
      Currency currency, AssetAllocationValue... values) {
    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocations(new ArrayList<>(List.of(values)));
    allocation.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    AssetAllocationWithCurrency wrapper = new AssetAllocationWithCurrency();
    wrapper.setAssetAllocation(allocation);
    wrapper.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    if (currency != null) {
      CurrencyDatapoint currencyDp = new CurrencyDatapoint();
      currencyDp.setValue(currency);
      wrapper.setCurrency(currencyDp);
    }
    return attributeResult(id, idType, wrapper);
  }

  private static AssetAllocationValue allocationValue(AssetAllocationRegionType type, String value) {
    return new AssetAllocationValue(type, new BigDecimal(value), new TreeSet<>());
  }

  private static <T> SecurityAttributeResult<T> attributeResult(String id, FiIdentifierType idType, T data) {
    SecurityIdentifier identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);
    SecurityAttributeResult<T> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(data);
    return result;
  }
}
