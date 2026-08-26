package ca.tangerine.pce.e2e;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static ca.tangerine.pce.e2e.BreakdownDistributions.assertDistribution;
import static ca.tangerine.pce.e2e.BreakdownDistributions.assertTotalsToOne;
import static ca.tangerine.pce.e2e.E2EPortfolios.cash;
import static ca.tangerine.pce.e2e.E2EPortfolios.etf;
import static ca.tangerine.pce.e2e.E2EPortfolios.fund;
import static ca.tangerine.pce.e2e.E2EPortfolios.gic;
import static ca.tangerine.pce.e2e.MicAttributeResponses.attributeResult;
import static ca.tangerine.pce.e2e.MicAttributeResponses.morningstarOnly;
import static ca.tangerine.pce.e2e.MicAttributeResponses.singleAttributeDispatcher;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.EquityCountryExposureResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocation;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocationValue;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

/**
 * End-to-end coverage for the {@code /equity-country-exposure} endpoint. Market Investment Catalogue answers with the
 * countries a security's equity sleeve is invested in; the metric rolls those onto the five {@link CountryRegionType}
 * buckets through the {@code country-allocation-mapping.json} table and reports one distribution for the portfolio.
 *
 * <p>
 * The two facts worth pinning at this level are the rollup itself — a portfolio whose funds name Japan, China and
 * Germany must come back as developed and emerging regions rather than as countries — and what happens to a holding
 * Market Investment Catalogue has no country data for: its weight leaves the reported distribution entirely and the
 * remaining buckets are rescaled over the hole, which only a warning tells the client about.
 *
 * <p>
 * Every fixture is denominated in CAD, and not for convenience: the {@code EQUITY_COUNTRY_ALLOCATION} datapoint carries
 * no currency at all, so {@code EquityCountryExposureService#currencyOf} returns {@code null} for every security and
 * their values are weighted unconverted (see the TODO there). A multi-currency portfolio is therefore weighted wrongly
 * by construction today, and no expectation written here could be both correct and green — the currency-bearing
 * holdings, cash and the GIC, are CAD for the same reason.
 */
@Tag("e2e")
class EquityCountryExposureE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String NORTH_AMERICAN_FUND = "F00000MP5F";
  private static final String DEVELOPED_FUND = "F00000TR21";
  private static final String WORLD_ETF = "XAW";

  @Override
  protected String metricPath() {
    return CalculationMetric.EQUITY_COUNTRY_EXPOSURE.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(exposureCommand(fund(NORTH_AMERICAN_FUND, 50_000), etf(WORLD_ETF, 50_000)));
  }

  /**
   * A portfolio shaped like a client's rather than the minimum the shared scenario needs: two funds and an ETF that
   * between them name a country from every mapped region, plus cash and a GIC. The cash and the GIC are why the
   * denominator is worth asserting — they carry no equity sleeve, so they contribute no exposure, yet they are part of
   * the portfolio whose weights the metric divides by, and the reported distribution must still come back at 100%.
   *
   * <p>
   * Individual stocks are deliberately absent. Market Investment Catalogue serves the country breakdown for composite
   * securities only, so a stock arrives with no data and is reported as a gap — the subject of
   * {@link #shouldWarnAndRescaleOverTheHole_whenMarketInvestmentCatalogueHasNoCountryAllocation()} rather than of the
   * positive case.
   */
  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(exposureCommand(
        fund(NORTH_AMERICAN_FUND, 40_000),
        fund(DEVELOPED_FUND, 20_000),
        etf(WORLD_ETF, 20_000),
        cash("CASH-CAD", Currency.CAD, 10_000),
        gic("GIC-RBC-3Y", Currency.CAD, 10_000, 1095)));
  }

  @Override
  protected String micPositiveResponseBody() {
    return writeJson(positiveScenarioRows());
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds five
   * across two identifier types, and how many attribute calls the fetcher batches them into is an implementation
   * detail. A dispatcher answers all of them, and answers them only on this metric's attribute path — which is what
   * keeps the declared {@code requiredAttribute()} honest.
   */
  @Override
  protected void enqueueForPositiveMicScenario() {
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.EQUITY_COUNTRY_ALLOCATION, positiveScenarioRows()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(fund(NORTH_AMERICAN_FUND, 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload, derived by hand from the request above. The five holdings total 100 000, so they weigh
   * 0.4 / 0.2 / 0.2 / 0.1 / 0.1, and the cash and GIC weights carry no exposure. Net products are therefore CANADA
   * 0.4·0.60 = 0.24, UNITED_STATES 0.4·0.40 = 0.16, INTERNATIONAL_DEVELOPED 0.2·1.00 (Japan) + 0.2·0.50 (Germany) =
   * 0.30 and EMERGING_MARKET 0.2·0.50 (China) = 0.10 — 0.80 in total, being the equity-bearing four fifths of the
   * portfolio. The reported distribution is that rescaled to 100%: 0.30 / 0.20 / 0.375 / 0.125.
   *
   * <p>
   * OTHER is asserted as an exact zero rather than ignored: the only country the mapping table puts there is
   * Supranational, and a country quietly landing there instead of in its region is precisely the defect the table
   * exists to prevent.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    EquityCountryExposureResult result = readJson(responseBody, EquityCountryExposureResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertDistribution(result.getEquityCountryExposure(), CountryRegionType.class, Map.of(
        CountryRegionType.CANADA, "0.30",
        CountryRegionType.UNITED_STATES, "0.20",
        CountryRegionType.INTERNATIONAL_DEVELOPED, "0.375",
        CountryRegionType.EMERGING_MARKET, "0.125"));
    assertTotalsToOne(result.getEquityCountryExposure());
  }

  /**
   * The two shapes a data gap arrives in — a security Market Investment Catalogue has no row for at all, and a row it
   * serves with no allocations on it — are the same gap to the client, and both have to be reported. What the reported
   * numbers then mean is the point of the assertion: the missing holdings' weight does not go to a bucket of its own
   * (this taxonomy has no UNKNOWN), it leaves the distribution, and what remains is rescaled to 100%. The fund that
   * does carry data holds half the portfolio and comes back describing all of it.
   */
  @Test
  void shouldWarnAndRescaleOverTheHole_whenMarketInvestmentCatalogueHasNoCountryAllocation() {
    PortfolioHolding allocated = fund(NORTH_AMERICAN_FUND, 50_000);
    PortfolioHolding withoutAllocations = fund(DEVELOPED_FUND, 30_000);
    PortfolioHolding unknownToMarketInvestmentCatalogue = etf(WORLD_ETF, 20_000);
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.EQUITY_COUNTRY_ALLOCATION, List.of(
            countryRow(NORTH_AMERICAN_FUND, FiIdentifierType.MORNINGSTAR_ID,
                countryValue(Country.CANADA, "0.60"),
                countryValue(Country.USA, "0.40")),
            emptyCountryRow(DEVELOPED_FUND, FiIdentifierType.MORNINGSTAR_ID))));

    var response = postCalculation(writeJson(
        exposureCommand(allocated, withoutAllocations, unknownToMarketInvestmentCatalogue)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    EquityCountryExposureResult result = readJson(response.responseBody(), EquityCountryExposureResult.class);
    assertMissingExposureWarnings(result.getWarnings(), withoutAllocations, unknownToMarketInvestmentCatalogue);
    assertDistribution(result.getEquityCountryExposure(), CountryRegionType.class, Map.of(
        CountryRegionType.CANADA, "0.60",
        CountryRegionType.UNITED_STATES, "0.40"));
    assertTotalsToOne(result.getEquityCountryExposure());
  }

  /**
   * When nothing in the portfolio has country data there is no distribution to rescale, and the metric says so
   * explicitly: every bucket of the taxonomy comes back present and null, rather than as zeros the client would render
   * as a real all-zero pie, or as an error. The warnings are then the only thing carrying information.
   */
  @Test
  void shouldReportEveryBucketAsNull_whenNoHoldingHasCountryAllocation() {
    PortfolioHolding firstUnknown = fund(NORTH_AMERICAN_FUND, 50_000);
    PortfolioHolding secondUnknown = etf(WORLD_ETF, 50_000);
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.EQUITY_COUNTRY_ALLOCATION, List.of()));

    var response = postCalculation(writeJson(exposureCommand(firstUnknown, secondUnknown)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    EquityCountryExposureResult result = readJson(response.responseBody(), EquityCountryExposureResult.class);
    assertMissingExposureWarnings(result.getWarnings(), firstUnknown, secondUnknown);
    assertThat(result.getEquityCountryExposure()).containsOnlyKeys(CountryRegionType.values());
    assertThat(result.getEquityCountryExposure().values()).containsOnlyNulls();
  }

  /**
   * Asserts the whole notification rather than its code: the holding the client has to act on is carried both in the
   * message and in the metadata, and a regression that drops the substituted id leaves a warning naming nothing.
   */
  private static void assertMissingExposureWarnings(List<Notification> warnings, PortfolioHolding... expectedFor) {
    assertThat(warnings).hasSize(expectedFor.length);
    for (int index = 0; index < expectedFor.length; index++) {
      String holdingId = expectedFor[index].getIdsString();
      Notification warning = warnings.get(index);
      assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.MISSING_EQUITY_COUNTRY_EXPOSURE);
      assertThat(warning.getMessage())
          .isEqualTo(ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE.getFormattedMessage(holdingId));
      assertThat(warning.getDescription()).isEqualTo(ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE.getDescription());
      assertThat(warning.getAction()).isEqualTo(ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE.getAction());
      assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
      assertThat(warning.getMetadata())
          .containsEntry("holdingId", holdingId)
          .containsEntry("param-1", holdingId);
    }
  }

  private static List<SecurityAttributeResult<CountryAllocation>> positiveScenarioRows() {
    return List.of(
        countryRow(NORTH_AMERICAN_FUND, FiIdentifierType.MORNINGSTAR_ID,
            countryValue(Country.CANADA, "0.60"),
            countryValue(Country.USA, "0.40")),
        countryRow(DEVELOPED_FUND, FiIdentifierType.MORNINGSTAR_ID,
            countryValue(Country.JAPAN, "1.00")),
        countryRow(WORLD_ETF, FiIdentifierType.TICKER,
            countryValue(Country.CHINA, "0.50"),
            countryValue(Country.GERMANY, "0.50")));
  }

  private static PortfolioHoldingsCommand exposureCommand(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.EQUITY_COUNTRY_EXPOSURE)
        .holdings(List.of(holdings))
        .dataProviders(morningstarOnly())
        .build();
  }

  private static SecurityAttributeResult<CountryAllocation> countryRow(String id, FiIdentifierType idType,
      CountryAllocationValue... values) {
    CountryAllocation allocation = new CountryAllocation();
    allocation.setAllocations(List.of(values));
    allocation.setDataProviders(morningstarOnly());
    return attributeResult(id, idType, allocation);
  }

  /**
   * The row Market Investment Catalogue serves for a security that declares the datapoint but has no countries on it —
   * the datapoint is assembled from whichever of its columns the security's table carries, so an allocation-less row is
   * a shape the client actually meets, distinct from no row at all.
   */
  private static SecurityAttributeResult<CountryAllocation> emptyCountryRow(String id, FiIdentifierType idType) {
    CountryAllocation allocation = new CountryAllocation();
    allocation.setDataProviders(morningstarOnly());
    return attributeResult(id, idType, allocation);
  }

  private static CountryAllocationValue countryValue(Country country, String value) {
    return new CountryAllocationValue(country, new BigDecimal(value), new TreeSet<>(), null);
  }
}
