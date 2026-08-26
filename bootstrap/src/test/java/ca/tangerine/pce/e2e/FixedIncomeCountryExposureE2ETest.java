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
import static ca.tangerine.pce.e2e.E2EPortfolios.stock;
import static ca.tangerine.pce.e2e.MicAttributeResponses.attributeResult;
import static ca.tangerine.pce.e2e.MicAttributeResponses.morningstarOnly;
import static ca.tangerine.pce.e2e.MicAttributeResponses.singleAttributeDispatcher;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.CountryExposureResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocation;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocationValue;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

/**
 * End-to-end coverage for the {@code /fixed-income-country-exposure} endpoint. It reads the whole-security
 * {@code COUNTRY_ALLOCATION} datapoint — the one issuer-country breakdown Market Investment Catalogue publishes per
 * security — and rolls it onto the five {@link CountryRegionType} buckets, so what this test pins over the wire is the
 * taxonomy the bond world needs and the equity one does not: an exposure with no country at all.
 *
 * <p>
 * Supranational issuers (the World Bank, the EIB) are routine in a bond portfolio and are not a data gap — they belong
 * in OTHER with their weight intact. {@code FixedIncomeCountryExposureServiceTest} pins that mapping on the domain
 * side; what only this level can show is that the vendor's non-country code survives serialization, the attribute fetch
 * and the response mapping rather than being dropped somewhere between them.
 *
 * <p>
 * Every fixture is denominated in CAD: {@code CountryExposure} carries no currency, so
 * {@code FixedIncomeCountryExposureService#currencyOf} returns {@code null} for every security and values are weighted
 * unconverted (see the TODO there). A multi-currency portfolio is weighted wrongly by construction today, so no
 * expectation here could be both correct and green.
 */
@Tag("e2e")
class FixedIncomeCountryExposureE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String CANADIAN_BOND_FUND = "F00000BND1";
  private static final String GLOBAL_BOND_FUND = "F00000BND2";
  private static final String BOND_ETF = "XBB";

  @Override
  protected String metricPath() {
    return CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(exposureCommand(fund(CANADIAN_BOND_FUND, 50_000), etf(BOND_ETF, 50_000)));
  }

  /**
   * A portfolio shaped like a client's fixed-income sleeve rather than the minimum the shared scenario needs: a
   * domestic bond fund, a bond ETF holding supranational paper, a global bond fund reaching a developed and an emerging
   * market, plus the cash and the GIC that carry no issuer country and so contribute no exposure while still being part
   * of the portfolio the weights are taken over.
   */
  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(exposureCommand(
        fund(CANADIAN_BOND_FUND, 40_000),
        etf(BOND_ETF, 30_000),
        fund(GLOBAL_BOND_FUND, 10_000),
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
   * detail. The dispatcher answers all of them, and only on this metric's attribute path — which is what keeps the
   * declared {@code requiredAttribute()} honest, and this metric reads the whole-security datapoint rather than a
   * fixed-income-sleeve one of its own.
   */
  @Override
  protected void enqueueForPositiveMicScenario() {
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.COUNTRY_ALLOCATION, positiveScenarioRows()));
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
   * The full expected payload, derived by hand from the request above. The five holdings total 100 000, so they weigh
   * 0.4 / 0.3 / 0.1 / 0.1 / 0.1, and the cash and GIC weights carry no exposure. Net products: CANADA 0.4·0.70 +
   * 0.3·0.50 = 0.43, UNITED_STATES 0.4·0.30 = 0.12, OTHER 0.3·0.50 (supranational) = 0.15, INTERNATIONAL_DEVELOPED
   * 0.1·0.50 (Japan) = 0.05 and EMERGING_MARKET 0.1·0.50 (Brazil) = 0.05 — 0.80 in total, being the country-bearing
   * four fifths of the portfolio, rescaled to 100%: 0.5375 / 0.15 / 0.1875 / 0.0625 / 0.0625.
   *
   * <p>
   * OTHER carrying 18.75% is the assertion that matters most here: it is a fifth of the reported pie, and a regression
   * that treated the supranational bucket as an unmapped country would delete it and inflate every other bucket rather
   * than fail visibly.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    CountryExposureResult result = readJson(responseBody, CountryExposureResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertDistribution(result.getCountryExposure(), CountryRegionType.class, Map.of(
        CountryRegionType.CANADA, "0.5375",
        CountryRegionType.UNITED_STATES, "0.15",
        CountryRegionType.OTHER, "0.1875",
        CountryRegionType.INTERNATIONAL_DEVELOPED, "0.0625",
        CountryRegionType.EMERGING_MARKET, "0.0625"));
    assertTotalsToOne(result.getCountryExposure());
  }

  /**
   * This metric asks for the whole-security country datapoint and does not scope itself to the bond-bearing holdings,
   * so an individual stock in the portfolio is not excluded from the breakdown — it is carried into it and reported as
   * a gap, because Market Investment Catalogue publishes the country breakdown for composite securities only. The
   * client therefore gets a warning naming the stock rather than a silently smaller pie, and the fund that does carry
   * data describes the whole of the reported distribution.
   */
  @Test
  void shouldWarnAndRescaleOverTheHole_whenTheHoldingHasNoCountryAllocation() {
    PortfolioHolding bondFund = fund(CANADIAN_BOND_FUND, FinancialInstrumentType.MUTUAL_FUND, 60_000);
    PortfolioHolding individualStock = stock("RY.TO", "TSX", 40_000);
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.COUNTRY_ALLOCATION, List.of(
            countryRow(CANADIAN_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID,
                countryValue(Country.CANADA, "0.80"),
                countryValue(Country.SUPRANATIONAL, "0.20")))));

    var response = postCalculation(writeJson(exposureCommand(bondFund, individualStock)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    CountryExposureResult result = readJson(response.responseBody(), CountryExposureResult.class);
    assertMissingExposureWarning(result.getWarnings(), individualStock);
    assertDistribution(result.getCountryExposure(), CountryRegionType.class, Map.of(
        CountryRegionType.CANADA, "0.80",
        CountryRegionType.OTHER, "0.20"));
    assertTotalsToOne(result.getCountryExposure());
  }

  /**
   * Asserts the whole notification rather than its code: the holding the client has to act on is carried both in the
   * message and in the metadata, and a regression that drops the substituted id leaves a warning naming nothing.
   */
  private static void assertMissingExposureWarning(List<Notification> warnings, PortfolioHolding expectedFor) {
    String holdingId = expectedFor.getIdsString();
    assertThat(warnings).hasSize(1);
    Notification warning = warnings.getFirst();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.MISSING_BOND_COUNTRY_EXPOSURE);
    assertThat(warning.getMessage())
        .isEqualTo(ErrorCode.MISSING_BOND_COUNTRY_EXPOSURE.getFormattedMessage(holdingId));
    assertThat(warning.getDescription()).isEqualTo(ErrorCode.MISSING_BOND_COUNTRY_EXPOSURE.getDescription());
    assertThat(warning.getAction()).isEqualTo(ErrorCode.MISSING_BOND_COUNTRY_EXPOSURE.getAction());
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holdingId)
        .containsEntry("param-1", holdingId);
  }

  private static List<SecurityAttributeResult<CountryAllocation>> positiveScenarioRows() {
    return List.of(
        countryRow(CANADIAN_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID,
            countryValue(Country.CANADA, "0.70"),
            countryValue(Country.USA, "0.30")),
        countryRow(BOND_ETF, FiIdentifierType.TICKER,
            countryValue(Country.CANADA, "0.50"),
            countryValue(Country.SUPRANATIONAL, "0.50")),
        countryRow(GLOBAL_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID,
            countryValue(Country.JAPAN, "0.50"),
            countryValue(Country.BRAZIL, "0.50")));
  }

  private static PortfolioHoldingsCommand exposureCommand(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE)
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

  private static CountryAllocationValue countryValue(Country country, String value) {
    return new CountryAllocationValue(country, new BigDecimal(value), new TreeSet<>(), null);
  }
}
