package ca.tangerine.pce.e2e;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static ca.tangerine.pce.e2e.BreakdownDistributions.assertDistribution;
import static ca.tangerine.pce.e2e.BreakdownDistributions.assertTotalsToOne;
import static ca.tangerine.pce.e2e.E2EPortfolios.cash;
import static ca.tangerine.pce.e2e.E2EPortfolios.etf;
import static ca.tangerine.pce.e2e.E2EPortfolios.fund;
import static ca.tangerine.pce.e2e.MicAttributeResponses.attributeResult;
import static ca.tangerine.pce.e2e.MicAttributeResponses.currencyDatapoint;
import static ca.tangerine.pce.e2e.MicAttributeResponses.morningstarOnly;
import static ca.tangerine.pce.e2e.MicAttributeResponses.singleAttributeDispatcher;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.allocation.FixedIncomeSectorResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationTypeValue;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;
import okhttp3.mockwebserver.MockWebServer;

/**
 * End-to-end coverage for the {@code /fixed-income-bond-sector} endpoint. Market Investment Catalogue classifies a
 * security's bond holdings into the eight-bucket sector taxonomy plus UNKNOWN, and this metric weights those per
 * holding into one portfolio distribution.
 *
 * <p>
 * Two things distinguish it from the country breakdowns and are what this class pins. First, its datapoint carries the
 * currency the security is quoted in, so a portfolio mixing currencies is weighted on comparable values — the positive
 * scenario holds a USD ETF for exactly that reason, and the expected percentages are wrong by a third if the conversion
 * is skipped. Second, a holding whose sectors are missing is not dropped from the distribution: it keeps its weight in
 * an UNKNOWN bucket, so the client can see how much of the portfolio the breakdown does not describe.
 */
@Tag("e2e")
class FixedIncomeBondSectorE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String USD_CAD_RATE = "1.5000";
  private static final String CANADIAN_BOND_FUND = "F00000BND1";
  private static final String SECURITIZED_BOND_FUND = "F00000BND2";
  private static final String USD_BOND_ETF = "AGG";

  private static MockWebServer bocMockServer;

  /**
   * The USD holding is converted through this server rather than the live Bank of Canada endpoint, at one constant
   * rate, so the expected percentages are a property of the request instead of a function of the rate of the day.
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
    return CalculationMetric.FIXED_INCOME_BOND_SECTOR.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(bondSectorCommand(fund(CANADIAN_BOND_FUND, 50_000), etf(USD_BOND_ETF, 50_000)));
  }

  /**
   * A portfolio shaped like a client's fixed-income sleeve rather than the minimum the shared scenario needs: two bond
   * funds and a US-listed bond ETF quoted in USD, plus cash — which carries no bond sectors of its own yet is part of
   * the portfolio the weights are taken over.
   *
   * <p>
   * A security's currency is whatever Market Investment Catalogue reports on its attribute row, not something
   * {@link PortfolioHolding} carries, so which holding is USD is decided in {@link #micPositiveResponseBody()}.
   */
  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(bondSectorCommand(
        fund(CANADIAN_BOND_FUND, 40_000),
        etf(USD_BOND_ETF, 20_000),
        fund(SECURITIZED_BOND_FUND, 10_000),
        cash("CASH-CAD", Currency.CAD, 20_000)));
  }

  @Override
  protected String micPositiveResponseBody() {
    return writeJson(positiveScenarioRows());
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds four
   * across two identifier types, and how many attribute calls the fetcher batches them into is an implementation
   * detail. The dispatcher answers all of them, and only on this metric's attribute path — which is what keeps the
   * declared {@code requiredAttribute()} honest.
   */
  @Override
  protected void enqueueForPositiveMicScenario() {
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.FIXED_INCOME_SECTOR_ALLOCATION, positiveScenarioRows()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(fund(CANADIAN_BOND_FUND, 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload, derived by hand from the request above. The USD ETF enters the denominator at
   * {@value #USD_CAD_RATE}, so the portfolio is 40 000 + 30 000 + 10 000 + 20 000 = 100 000 CAD and the holdings weigh
   * 0.4 / 0.3 / 0.1 / 0.2, the cash weight carrying no sectors. Net products: GOVERNMENT_BONDS 0.4·0.60 = 0.24,
   * CORPORATE_BONDS 0.4·0.40 = 0.16, MORTGAGE_BACKED_SECURITIES 0.3·0.50 = 0.15, ST_INVESTMENTS 0.3·0.50 = 0.15 and
   * SECURITIZED_DEBT 0.1·1.00 = 0.10 — 0.80 in total, being the bond-bearing four fifths of the portfolio, rescaled to
   * 100%: 0.30 / 0.20 / 0.1875 / 0.1875 / 0.125.
   *
   * <p>
   * The USD ETF is what makes this more than an arithmetic restatement of the fixture: without the conversion it weighs
   * 20 000 of a 90 000 portfolio instead of 30 000 of 100 000, which moves every bucket.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    FixedIncomeSectorResult result = readJson(responseBody, FixedIncomeSectorResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertDistribution(result.getFixedIncomeSector(), FixedIncomeSectorAllocationType.class, Map.of(
        FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, "0.30",
        FixedIncomeSectorAllocationType.CORPORATE_BONDS, "0.20",
        FixedIncomeSectorAllocationType.MORTGAGE_BACKED_SECURITIES, "0.1875",
        FixedIncomeSectorAllocationType.ST_INVESTMENTS, "0.1875",
        FixedIncomeSectorAllocationType.SECURITIZED_DEBT, "0.125"));
    assertTotalsToOne(result.getFixedIncomeSector());
  }

  /**
   * The two data gaps this metric distinguishes, in one request: a security Market Investment Catalogue has no record
   * of at all ({@code SECURITY_NOT_FOUND_FOR_METRIC}) and one whose row it serves with no sectors on it
   * ({@code MISSING_FIXED_INCOME_BOND_SECTOR}). They are separate codes because they call for different action from the
   * caller — a wrong identifier versus a gap in the vendor's data — and both keep the holding's weight in UNKNOWN,
   * which is what tells the client that half of this portfolio is undescribed rather than showing a pie that quietly
   * describes only the other half.
   */
  @Test
  void shouldWarnAndBucketUnknown_whenSectorsAreMissing() {
    PortfolioHolding classified = fund(CANADIAN_BOND_FUND, 50_000);
    PortfolioHolding unknownToMarketInvestmentCatalogue = fund(SECURITIZED_BOND_FUND, 30_000);
    PortfolioHolding withoutSectors = etf(USD_BOND_ETF, 20_000);
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.FIXED_INCOME_SECTOR_ALLOCATION, List.of(
            sectorRow(CANADIAN_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                sectorValue(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, "1.00")),
            emptySectorRow(USD_BOND_ETF, FiIdentifierType.TICKER, Currency.CAD))));

    var response = postCalculation(writeJson(
        bondSectorCommand(classified, unknownToMarketInvestmentCatalogue, withoutSectors)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    FixedIncomeSectorResult result = readJson(response.responseBody(), FixedIncomeSectorResult.class);
    assertThat(result.getWarnings()).hasSize(2);
    assertSecurityNotFoundWarning(result.getWarnings().getFirst(), unknownToMarketInvestmentCatalogue);
    assertMissingSectorsWarning(result.getWarnings().get(1), withoutSectors);
    assertDistribution(result.getFixedIncomeSector(), FixedIncomeSectorAllocationType.class, Map.of(
        FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, "0.50",
        FixedIncomeSectorAllocationType.UNKNOWN, "0.50"));
    assertTotalsToOne(result.getFixedIncomeSector());
  }

  /**
   * Asserts the whole notification rather than its code. This one substitutes the metric name into the message and the
   * holding id into the metadata only, so a regression that swaps the two arguments produces a message naming a holding
   * where the metric belongs — which a code-only assertion would pass.
   */
  private static void assertSecurityNotFoundWarning(Notification warning, PortfolioHolding expectedFor) {
    String holdingId = expectedFor.getIdsString();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertThat(warning.getMessage()).isEqualTo(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC
        .getFormattedMessage(holdingId, CalculationMetric.FIXED_INCOME_BOND_SECTOR.getUserFriendlyName()));
    assertThat(warning.getDescription()).isEqualTo(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.getDescription());
    assertThat(warning.getAction()).isEqualTo(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.getAction());
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holdingId)
        .containsEntry("param-1", holdingId)
        .containsEntry("param-2", CalculationMetric.FIXED_INCOME_BOND_SECTOR.getUserFriendlyName());
  }

  private static void assertMissingSectorsWarning(Notification warning, PortfolioHolding expectedFor) {
    String holdingId = expectedFor.getIdsString();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.MISSING_FIXED_INCOME_BOND_SECTOR);
    assertThat(warning.getMessage())
        .isEqualTo(ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR.getFormattedMessage(holdingId));
    assertThat(warning.getDescription()).isEqualTo(ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR.getDescription());
    assertThat(warning.getAction()).isEqualTo(ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR.getAction());
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holdingId)
        .containsEntry("param-1", holdingId);
  }

  private static List<SecurityAttributeResult<FixedIncomeSectorAllocationWithCurrency>> positiveScenarioRows() {
    return List.of(
        sectorRow(CANADIAN_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
            sectorValue(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, "0.60"),
            sectorValue(FixedIncomeSectorAllocationType.CORPORATE_BONDS, "0.40")),
        sectorRow(USD_BOND_ETF, FiIdentifierType.TICKER, Currency.USD,
            sectorValue(FixedIncomeSectorAllocationType.MORTGAGE_BACKED_SECURITIES, "0.50"),
            sectorValue(FixedIncomeSectorAllocationType.ST_INVESTMENTS, "0.50")),
        sectorRow(SECURITIZED_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
            sectorValue(FixedIncomeSectorAllocationType.SECURITIZED_DEBT, "1.00")));
  }

  private static PortfolioHoldingsCommand bondSectorCommand(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.FIXED_INCOME_BOND_SECTOR)
        .holdings(List.of(holdings))
        .dataProviders(morningstarOnly())
        .build();
  }

  private static SecurityAttributeResult<FixedIncomeSectorAllocationWithCurrency> sectorRow(String id,
      FiIdentifierType idType, Currency currency, FixedIncomeSectorAllocationTypeValue... values) {
    FixedIncomeSectorAllocation allocation = new FixedIncomeSectorAllocation();
    allocation.setAllocations(List.of(values));
    allocation.setDataProviders(morningstarOnly());

    FixedIncomeSectorAllocationWithCurrency row = new FixedIncomeSectorAllocationWithCurrency();
    row.setFixedIncomeSectorAllocation(allocation);
    row.setCurrency(currencyDatapoint(currency));
    row.setDataProviders(morningstarOnly());
    return attributeResult(id, idType, row);
  }

  /**
   * The row Market Investment Catalogue serves for a security that declares the datapoint but has no sectors on it: the
   * datapoint is assembled from whichever of its columns the security's table carries, and every security carries
   * {@code currency}, so a currency-only row is a shape the caller actually meets — and one that must not be mistaken
   * for data.
   */
  private static SecurityAttributeResult<FixedIncomeSectorAllocationWithCurrency> emptySectorRow(String id,
      FiIdentifierType idType, Currency currency) {
    FixedIncomeSectorAllocationWithCurrency row = new FixedIncomeSectorAllocationWithCurrency();
    row.setCurrency(currencyDatapoint(currency));
    row.setDataProviders(morningstarOnly());
    return attributeResult(id, idType, row);
  }

  private static FixedIncomeSectorAllocationTypeValue sectorValue(FixedIncomeSectorAllocationType type, String value) {
    return new FixedIncomeSectorAllocationTypeValue(type, new BigDecimal(value), new TreeSet<>(), null);
  }
}
