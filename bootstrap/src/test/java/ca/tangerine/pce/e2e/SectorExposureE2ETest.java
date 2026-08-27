package ca.tangerine.pce.e2e;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.cash;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.etfCa;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.fund;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.gic;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.stockCa;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.allocation.ConsolidatedSectorExposureResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorDatapoint;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocation;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationValue;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * End-to-end coverage for the {@code /sector-exposure} endpoint. The point of the metric is that one distribution
 * covers the whole portfolio, so the assertions here are about the buckets totalling 100% across every holding type —
 * including cash and GICs, which the per-sleeve sector metrics drop, and which Market Investment Catalogue has no
 * security for. The shared positive scenario carries all of them at once and in two currencies, since a breakdown that
 * is right per holding can still be wrong in the denominator.
 *
 * <p>
 * Since TMI-558 the Market Investment Catalogue fixtures carry the consolidated {@code SECTOR_ALLOCATION} vector —
 * equity sectors and bond buckets already sized against the whole security — rather than the two per-sleeve vectors
 * plus an asset mix this service used to reconcile itself.
 */
@Tag("e2e")
class SectorExposureE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String ATTRIBUTES_PATH = "/api/v1/wealth/securities/attributes";
  private static final String USD_CAD_RATE = "1.5000";
  private static final String BALANCED_ETF = "XBAL";
  private static final String BOND_ETF = "XBB";
  private static final String EQUITY_FUND = "F00000MP5F";
  private static final String SEGREGATED_FUND = "F00000TR21";

  /**
   * Held as a holding rather than a ticker so the warning assertion can name it through {@code getIdsString()} instead
   * of spelling out how a holding id is composed — for a {@code TICKER_MIC} identifier that is
   * {@code STOCK-<ticker>-<exchange>}, which is the identifier layer's business, not this metric's.
   */
  private static final PortfolioHolding STOCK = stockCa("RY.TO", "TSX", 15_000);

  private static MockWebServer bocMockServer;

  /**
   * The USD holdings in the positive scenario are converted through this server rather than through the live Bank of
   * Canada endpoint, at one constant rate, so the expected percentages are a property of the request instead of a
   * function of the rate of the day.
   */
  @BeforeAll
  static void startBocMockServer() throws IOException {
    bocMockServer = new MockWebServer();
    bocMockServer.setDispatcher(BocMockResponses.constantUsdCadRateDispatcher(USD_CAD_RATE));
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
    return CalculationMetric.SECTOR_EXPOSURE.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(sectorExposureCommand(CalculationMetric.SECTOR_EXPOSURE, etfCa(BALANCED_ETF, 50_000)));
  }

  /**
   * A portfolio shaped like a client's rather than the minimum the shared scenario needs: every holding type the metric
   * can meet, across two currencies. A mutual fund and a segregated fund by Morningstar id, a balanced ETF and a bond
   * ETF by ticker, an individual stock by ticker-and-exchange, cash, and two GICs on either side of the one-year line —
   * so the attribute path, the cash and GIC buckets Market Investment Catalogue has no security for, and the
   * unresolved-holding path all run in this one request, and FX weighting runs with them.
   *
   * <p>
   * A security's currency is whatever Market Investment Catalogue reports on its attribute row —
   * {@link PortfolioHolding} carries no currency of its own — so which holdings are USD is decided in
   * {@link #micPositiveResponseBody()}, while cash and GICs carry theirs on the holding.
   *
   * <p>
   * The stock is the holding that exercises both attributes at once, which is what one actually produces: Security
   * Master fills the consolidated vector for composite securities only — {@code SectorAllocationSetter} runs in the
   * fund, ETF, index and SMA chains — so a stock answers that attribute with its currency alone, while its sector comes
   * back on the scalar {@code EQUITY_SECTOR} one. The weight therefore reaches the sector rather than {@code UNKNOWN},
   * and the currency-only row must not be mistaken for data. A holding Market Investment Catalogue has no record of at
   * all is the other path, covered by
   * {@link #shouldWarnAndBucketUnknown_whenSecurityIsUnknownToMarketInvestmentCatalogue()}.
   */
  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(sectorExposureCommand(CalculationMetric.SECTOR_EXPOSURE,
        fund(EQUITY_FUND, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, 40_000),
        fund(SEGREGATED_FUND, FinancialInstrumentType.SEGREGATED_FUND, Country.CANADA, 20_000),
        etfCa(BALANCED_ETF, 30_000),
        etfCa(BOND_ETF, 20_000),
        STOCK,
        cash(Currency.USD, 10_000),
        gic(null, Currency.CAD, BigDecimal.valueOf(20_000), BigDecimal.valueOf(180)),
        gic(null, Currency.CAD, BigDecimal.valueOf(20_000), BigDecimal.valueOf(1095))));
  }

  /**
   * The four funds as Market Investment Catalogue publishes them since TMI-558: one vector per security, equity sectors
   * and bond buckets already sized against the whole security, each row carrying the currency its values are quoted in.
   * The segregated fund and the bond ETF are quoted in USD, so their weights are FX-converted while the two CAD rows
   * are not. The stock's row carries its currency and no distribution — see
   * {@link #requestBodyForPositiveMicScenario()}.
   */
  @Override
  protected String micPositiveResponseBody() {
    return compositeBody(
        List.of(
            sectorRow(EQUITY_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                sectorValue(SectorAllocationType.TECHNOLOGY, "0.4"),
                sectorValue(SectorAllocationType.HEALTHCARE, "0.2"),
                sectorValue(SectorAllocationType.GOVERNMENT_BONDS, "0.4")),
            sectorRow(SEGREGATED_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.USD,
                sectorValue(SectorAllocationType.ENERGY, "0.5"),
                sectorValue(SectorAllocationType.CORPORATE_BONDS, "0.5")),
            sectorRow(BALANCED_ETF, FiIdentifierType.TICKER, Currency.CAD,
                sectorValue(SectorAllocationType.FINANCIAL_SERVICES, "0.6"),
                sectorValue(SectorAllocationType.CORPORATE_BONDS, "0.4")),
            sectorRow(BOND_ETF, FiIdentifierType.TICKER, Currency.USD,
                sectorValue(SectorAllocationType.GOVERNMENT_BONDS, "1.0")),
            undistributedSectorRow(STOCK.getSecurityIdentifier(), Currency.CAD)),
        List.of(equitySectorRow(STOCK.getSecurityIdentifier(), EquitySectorAllocationType.INDUSTRIALS,
            Currency.CAD)));
  }

  /**
   * The shared positive scenario enqueues one response, which suffices for a single holding; this portfolio holds seven
   * across three identifier types, and how many attribute calls the fetcher batches them into is an implementation
   * detail. Answering every {@code /attributes} call from a dispatcher keeps the test about the metric rather than
   * about the batching.
   */
  @Override
  protected void enqueueForPositiveMicScenario() {
    micMockServer.setDispatcher(attributesDispatcher(micPositiveResponseBody()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(etfCa(BALANCED_ETF, 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload, derived by hand from the request above. Everything is in the denominator, cash and GICs
   * included, and the USD holdings enter it at {@value #USD_CAD_RATE}: 40 000 CAD mutual fund, 20 000 USD segregated
   * fund = 30 000, 30 000 CAD balanced ETF, 20 000 USD bond ETF = 30 000, 15 000 CAD stock, 10 000 USD cash = 15 000,
   * and two 20 000 CAD GICs — 200 000 CAD in total, so the eight holdings weigh 0.2 / 0.15 / 0.15 / 0.15 / 0.075 /
   * 0.075 / 0.1 / 0.1.
   *
   * <p>
   * TECHNOLOGY is 0.2·0.4 = 0.08; HEALTHCARE 0.2·0.2 = 0.04; GOVERNMENT_BONDS 0.2·0.4 + 0.15·1.0 = 0.23; ENERGY
   * 0.15·0.5 = 0.075; CORPORATE_BONDS 0.15·0.5 + 0.15·0.4 = 0.135; FINANCIAL_SERVICES 0.15·0.6 = 0.09; ST_INVESTMENTS
   * is the cash plus the 180-day GIC = 0.175; OTHER_BONDS is the three-year GIC = 0.1; INDUSTRIALS is the stock's whole
   * weight = 0.075, on its own scalar sector rather than in {@code UNKNOWN}. Nothing is unaccounted for, so there is no
   * warning — which is the point of the metric: the pie describes all the money in the portfolio. The two currencies
   * are what make this more than an arithmetic restatement of the fixture: drop the FX conversion and the USD holdings
   * weigh two thirds of what they should, which moves six of the nine buckets.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    ConsolidatedSectorExposureResult result = readJson(responseBody, ConsolidatedSectorExposureResult.class);

    assertDistribution(result, Map.of(
        SectorAllocationType.TECHNOLOGY, "0.08",
        SectorAllocationType.HEALTHCARE, "0.04",
        SectorAllocationType.ENERGY, "0.075",
        SectorAllocationType.FINANCIAL_SERVICES, "0.09",
        SectorAllocationType.INDUSTRIALS, "0.075",
        SectorAllocationType.GOVERNMENT_BONDS, "0.23",
        SectorAllocationType.CORPORATE_BONDS, "0.135",
        SectorAllocationType.ST_INVESTMENTS, "0.175",
        SectorAllocationType.OTHER_BONDS, "0.10"));

    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * A security Market Investment Catalogue has no record of takes its whole weight to UNKNOWN with a warning naming it,
   * so the donut still totals 100% and the client can tell how much of the portfolio the breakdown does not describe.
   * The one scenario kept here beyond the shared positive one: it is the only path where a warning has to survive the
   * wire, and the per-holding bucketing rules it shares with the resolved-but-undistributed case are pinned in
   * {@code SectorExposureServiceTest}.
   */
  @Test
  void shouldWarnAndBucketUnknown_whenSecurityIsUnknownToMarketInvestmentCatalogue() {
    enqueueMicMockResponse(compositeBody(List.of(sectorRow(BALANCED_ETF,
        sectorValue(SectorAllocationType.ENERGY, "1.0"))), List.of()));

    var response = postCalculation(writeJson(sectorExposureCommand(CalculationMetric.SECTOR_EXPOSURE,
        etfCa(BALANCED_ETF, 50_000),
        etfCa("MISSING", 50_000))));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    ConsolidatedSectorExposureResult result = readJson(response.responseBody(),
        ConsolidatedSectorExposureResult.class);
    assertThat(result.getWarnings()).hasSize(1);
    Notification warning = result.getWarnings().getFirst();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertThat(warning.getMessage()).isEqualTo(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC
        .getFormattedMessage("MISSING", CalculationMetric.SECTOR_EXPOSURE.getUserFriendlyName()));
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertDistribution(result, Map.of(
        SectorAllocationType.ENERGY, "0.5",
        SectorAllocationType.UNKNOWN, "0.5"));
  }

  /**
   * Asserts the whole payload rather than the buckets a scenario happens to populate: every bucket of the enum is
   * present, the listed ones carry exactly the expected value, every other one is exactly zero, and the distribution
   * totals 100%.
   *
   * <p>
   * Exact rather than within a tolerance. This metric deliberately does not renormalise — the buckets are the weighted
   * sums themselves — so every expectation in this class terminates in base 10, and a rounding regression at the fourth
   * decimal should fail here rather than slip under a tolerance while the client renders two. The zeros matter for the
   * same reason: a bucket quietly picking up weight is exactly the defect this metric was written to fix.
   */
  private static void assertDistribution(ConsolidatedSectorExposureResult result,
      Map<SectorAllocationType, String> expected) {
    Map<SectorAllocationType, BigDecimal> actual = result.getSectorExposure();
    assertThat(actual).containsOnlyKeys(SectorAllocationType.values());
    for (SectorAllocationType bucket : SectorAllocationType.values()) {
      assertThat(actual.get(bucket)).as("bucket %s", bucket)
          .isEqualByComparingTo(new BigDecimal(expected.getOrDefault(bucket, "0")));
    }
    assertThat(total(result)).isEqualByComparingTo(BigDecimal.ONE);
  }

  private static BigDecimal total(ConsolidatedSectorExposureResult result) {
    return result.getSectorExposure().values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static PortfolioHoldingsCommand sectorExposureCommand(CalculationMetric metric,
      PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(metric)
        .dataProviders(List.of(DataProvider.MORNINGSTAR))
        .holdings(List.of(holdings))
        .build();
  }

  private static SecurityAttributeResult<SectorAllocationWithCurrency> sectorRow(String ticker,
      SectorAllocationValue... values) {
    return sectorRow(ticker, FiIdentifierType.TICKER, Currency.CAD, values);
  }

  /**
   * A row exactly as Market Investment Catalogue serves the attribute for a security whose consolidated vector it does
   * not fill: the currency alone, with no {@code sectorAllocation} at all — not an empty one — since the attribute is
   * assembled from whichever of its fields the security's table carries.
   */
  private static SecurityAttributeResult<SectorAllocationWithCurrency> undistributedSectorRow(
      SecurityIdentifier identifier, Currency currency) {
    SectorAllocationWithCurrency wrapper = new SectorAllocationWithCurrency();
    wrapper.setCurrency(currency(currency));
    wrapper.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return securityAttributeResult(identifier, wrapper);
  }

  private static SecurityAttributeResult<SectorAllocationWithCurrency> sectorRow(String id, FiIdentifierType idType,
      Currency currency, SectorAllocationValue... values) {
    SectorAllocation allocation = new SectorAllocation();
    allocation.setAllocations(new ArrayList<>(List.of(values)));
    allocation.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    SectorAllocationWithCurrency wrapper = new SectorAllocationWithCurrency();
    wrapper.setSectorAllocation(allocation);
    wrapper.setCurrency(currency(currency));
    wrapper.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return securityAttributeResult(new SecurityIdentifier(id, idType), wrapper);
  }

  private static Dispatcher attributesDispatcher(String body) {
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath();
        if (path != null && path.contains(ATTRIBUTES_PATH)) {
          return new MockResponse()
              .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .setBody(body);
        }
        return new MockResponse().setResponseCode(404);
      }
    };
  }

  private static SectorAllocationValue sectorValue(SectorAllocationType type, String value) {
    return new SectorAllocationValue(type, new BigDecimal(value), new TreeSet<>(), null);
  }

  private static String compositeBody(List<SecurityAttributeResult<SectorAllocationWithCurrency>> consolidated,
      List<SecurityAttributeResult<EquitySectorWithCurrency>> scalar) {
    return writeJson(Map.of(
        CompositeSecurityAttribute.SECTOR_ALLOCATION, consolidated,
        CompositeSecurityAttribute.EQUITY_SECTOR, scalar));
  }

  private static SecurityAttributeResult<EquitySectorWithCurrency> equitySectorRow(SecurityIdentifier identifier,
      EquitySectorAllocationType sector, Currency quotedIn) {
    EquitySectorDatapoint datapoint = new EquitySectorDatapoint();
    datapoint.setEquitySector(sector);
    datapoint.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    EquitySectorWithCurrency wrapper = new EquitySectorWithCurrency();
    wrapper.setSector(datapoint);
    wrapper.setCurrency(currency(quotedIn));
    wrapper.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return securityAttributeResult(identifier, wrapper);
  }

  private static CurrencyDatapoint currency(Currency currency) {
    CurrencyDatapoint datapoint = new CurrencyDatapoint();
    datapoint.setValue(currency);
    return datapoint;
  }

}
