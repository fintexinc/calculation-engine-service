package ca.tangerine.pce.e2e;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.e2e.BreakdownDistributions.assertDistribution;
import static ca.tangerine.pce.e2e.BreakdownDistributions.assertTotalsToOne;
import static ca.tangerine.pce.e2e.E2EPortfolios.bond;
import static ca.tangerine.pce.e2e.E2EPortfolios.cash;
import static ca.tangerine.pce.e2e.E2EPortfolios.etf;
import static ca.tangerine.pce.e2e.E2EPortfolios.fund;
import static ca.tangerine.pce.e2e.E2EPortfolios.gic;
import static ca.tangerine.pce.e2e.E2EPortfolios.stock;
import static ca.tangerine.pce.e2e.MicAttributeResponses.compositeDispatcher;
import static ca.tangerine.pce.e2e.MicAttributeResponses.emptyGeographicAllocationRow;
import static ca.tangerine.pce.e2e.MicAttributeResponses.geographicAllocationRow;
import static ca.tangerine.pce.e2e.MicAttributeResponses.geographyRow;
import static ca.tangerine.pce.e2e.MicAttributeResponses.morningstarOnly;
import static ca.tangerine.pce.e2e.MicAttributeResponses.regionValue;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage for the {@code /fixed-income-geographic-exposure} endpoint — the fixed-income sleeve of the
 * region breakdown, and the mirror image of the equity one: cash, GICs and individual stocks are excluded from the
 * breakdown and from the weight denominator, while an individual bond is carried into it.
 *
 * <p>
 * That boundary is what this class pins, since it is the only thing distinguishing this metric from its two siblings.
 * The positive scenario holds a stock worth more than the entire bond sleeve, so a regression that let the equity side
 * into the denominator would halve every reported bucket rather than fail on a total.
 *
 * <p>
 * Every attribute row is denominated in CAD on purpose: the FX rate would otherwise come from the live Bank of Canada
 * endpoint and the expected percentages would depend on the rate of the day.
 */
@Tag("e2e")
class FixedIncomeGeographicExposureE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String GLOBAL_BOND_FUND = "F00000FI01";
  private static final String EUROPEAN_BOND_FUND = "F00000FI02";
  private static final String BOND_ETF = "XBB";
  private static final String GOVERNMENT_BOND = "CA135087P493";

  @Override
  protected String metricPath() {
    return CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(exposureCommand(fund(GLOBAL_BOND_FUND, 50_000), etf(BOND_ETF, 50_000)));
  }

  /**
   * A portfolio shaped like a client's rather than the minimum the shared scenario needs: two bond funds and a bond ETF
   * — the bond-bearing holdings — alongside an individual stock, cash and a GIC, which this metric scopes out of both
   * the breakdown and its denominator. The stock is the largest single holding in the request for exactly that reason.
   */
  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(exposureCommand(
        fund(GLOBAL_BOND_FUND, 40_000),
        etf(BOND_ETF, 20_000),
        fund(EUROPEAN_BOND_FUND, 20_000),
        stock("RY.TO", "TSX", 50_000),
        cash("CASH-CAD", Currency.CAD, 30_000),
        gic("GIC-RBC-3Y", Currency.CAD, 20_000, 1095)));
  }

  @Override
  protected String micPositiveResponseBody() {
    return writeJson(positiveScenarioRows());
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds six
   * across three identifier types, and how many attribute calls the fetcher batches them into is an implementation
   * detail. Answering every {@code /attributes} call from a dispatcher keeps the test about the metric rather than
   * about the batching.
   */
  @Override
  protected void enqueueForPositiveMicScenario() {
    micMockServer.setDispatcher(compositeDispatcher(positiveScenarioRows()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(fund(GLOBAL_BOND_FUND, 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload, derived by hand from the request above. Only the three bond-bearing holdings form the
   * denominator — 80 000 of the portfolio's 180 000 — so they weigh 0.5 / 0.25 / 0.25. US is 0.5·0.50 = 0.25; CANADA is
   * 0.5·0.50 + 0.25·1.00 = 0.50; EUROPE is 0.25·0.50 = 0.125; ASIA is 0.25·0.50 = 0.125.
   *
   * <p>
   * The distribution totals 100% before any rescaling, which is what makes the sleeve boundary visible: had the stock,
   * the cash or the GIC entered the denominator, every bucket would come back smaller and the total would still be 100%
   * after the rescale — a defect no total-only assertion could catch.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    FixedIncomeGeographicExposureResult result = readJson(responseBody,
        FixedIncomeGeographicExposureResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertDistribution(result.getGeographicExposure(), GeographicRegionType.class, Map.of(
        GeographicRegionType.US, "0.25",
        GeographicRegionType.CANADA, "0.50",
        GeographicRegionType.EUROPE, "0.125",
        GeographicRegionType.ASIA, "0.125"));
    assertTotalsToOne(result.getGeographicExposure());
  }

  /**
   * An individual bond is in this sleeve, and a stock is not — the two halves of the boundary asserted in one request.
   * The bond has no row, so it takes its weight into UNKNOWN with a {@code SECURITY_NOT_FOUND_FOR_METRIC} warning
   * naming it; the fund whose row carries no regions does the same with
   * {@code MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE}; and the stock, which has no row either, produces no warning at
   * all because it was never part of this breakdown. A regression that widened the sleeve would show up as a third
   * warning naming the stock.
   */
  @Test
  void shouldWarnForBondsOnly_whenRegionsAreMissingAndAStockIsHeldAlongside() {
    PortfolioHolding allocatedFund = fund(GLOBAL_BOND_FUND, 50_000);
    PortfolioHolding fundWithoutRegions = fund(EUROPEAN_BOND_FUND, 25_000);
    PortfolioHolding individualBond = bond(GOVERNMENT_BOND, 25_000);
    PortfolioHolding excludedStock = stock("RY.TO", "TSX", 100_000);
    micMockServer.setDispatcher(compositeDispatcher(Map.of(
        CompositeSecurityAttribute.FIXED_INCOME_GEOGRAPHIC_ALLOCATION, List.of(
            geographicAllocationRow(GLOBAL_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.CANADA, "1.00")),
            emptyGeographicAllocationRow(EUROPEAN_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD)),
        CompositeSecurityAttribute.GEOGRAPHY, List.of())));

    var response = postCalculation(writeJson(
        exposureCommand(allocatedFund, fundWithoutRegions, individualBond, excludedStock)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    FixedIncomeGeographicExposureResult result = readJson(response.responseBody(),
        FixedIncomeGeographicExposureResult.class);
    assertThat(result.getWarnings()).hasSize(2);
    assertMissingRegionsWarning(result.getWarnings().getFirst(), fundWithoutRegions);
    assertSecurityNotFoundWarning(result.getWarnings().get(1), individualBond);
    assertThat(result.getWarnings())
        .as("a stock is not part of the fixed-income sleeve, so it cannot be reported as missing from it")
        .noneMatch(warning -> excludedStock.getIdsString().equals(warning.getMetadata().get("holdingId")));
    assertDistribution(result.getGeographicExposure(), GeographicRegionType.class, Map.of(
        GeographicRegionType.CANADA, "0.50",
        GeographicRegionType.UNKNOWN, "0.50"));
    assertTotalsToOne(result.getGeographicExposure());
  }

  private static void assertMissingRegionsWarning(Notification warning, PortfolioHolding expectedFor) {
    String holdingId = expectedFor.getIdsString();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE);
    assertThat(warning.getMessage())
        .isEqualTo(ErrorCode.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE.getFormattedMessage(holdingId));
    assertThat(warning.getDescription())
        .isEqualTo(ErrorCode.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE.getDescription());
    assertThat(warning.getAction()).isEqualTo(ErrorCode.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE.getAction());
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holdingId)
        .containsEntry("param-1", holdingId);
  }

  private static void assertSecurityNotFoundWarning(Notification warning, PortfolioHolding expectedFor) {
    String holdingId = expectedFor.getIdsString();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertThat(warning.getMessage()).isEqualTo(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC
        .getFormattedMessage(holdingId, CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE.getUserFriendlyName()));
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holdingId)
        .containsEntry("param-2", CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE.getUserFriendlyName());
  }

  private static Map<CompositeSecurityAttribute, List<? extends SecurityAttributeResult<?>>> positiveScenarioRows() {
    return Map.of(
        CompositeSecurityAttribute.FIXED_INCOME_GEOGRAPHIC_ALLOCATION, List.of(
            geographicAllocationRow(GLOBAL_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.US, "0.50"),
                regionValue(GeographicRegionType.CANADA, "0.50")),
            geographicAllocationRow(BOND_ETF, FiIdentifierType.TICKER, Currency.CAD,
                regionValue(GeographicRegionType.CANADA, "1.00")),
            geographicAllocationRow(EUROPEAN_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.EUROPE, "0.50"),
                regionValue(GeographicRegionType.ASIA, "0.50"))),
        CompositeSecurityAttribute.GEOGRAPHY, List.of(
            geographyRow(GLOBAL_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, null, null, Currency.CAD),
            geographyRow(BOND_ETF, FiIdentifierType.TICKER, null, null, Currency.CAD),
            geographyRow(EUROPEAN_BOND_FUND, FiIdentifierType.MORNINGSTAR_ID, null, null, Currency.CAD)));
  }

  private static PortfolioHoldingsCommand exposureCommand(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE)
        .holdings(List.of(holdings))
        .dataProviders(morningstarOnly())
        .build();
  }
}
