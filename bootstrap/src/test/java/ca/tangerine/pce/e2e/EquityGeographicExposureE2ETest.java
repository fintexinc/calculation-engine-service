package ca.tangerine.pce.e2e;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.EquityGeographicExposureResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.Country;
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
 * End-to-end coverage for the {@code /equity-geographic-exposure} endpoint — the equity sleeve of the region breakdown.
 * Funds and ETFs answer with a region vector for their equity sleeve; an individual company belongs to exactly one
 * region and is resolved through its {@code GEOGRAPHY} row instead, which is why this metric reads two attributes.
 *
 * <p>
 * What only this level can show, and what the consolidated {@code GeographicExposureE2ETest} cannot, is the sleeve
 * boundary: bonds, cash and GICs are excluded from the breakdown <em>and</em> from the weight denominator, so the
 * reported percentages describe the equity part of the portfolio as actually held rather than the whole of it. The
 * positive scenario therefore holds all three of them at values large enough that including any one would move every
 * bucket.
 *
 * <p>
 * Every attribute row is denominated in CAD on purpose: the FX rate would otherwise come from the live Bank of Canada
 * endpoint and the expected percentages would depend on the rate of the day. Multi-currency weighting for this family
 * is pinned in {@code EquityGeographicExposureServiceTest}, where the rate is stubbed.
 */
@Tag("e2e")
class EquityGeographicExposureE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String GLOBAL_EQUITY_FUND = "F00000EQ01";
  private static final String CANADIAN_EQUITY_FUND = "F00000EQ02";
  private static final String WORLD_ETF = "XAW";
  private static final String STOCK_TICKER = "RY.TO";
  private static final String STOCK_EXCHANGE = "TSX";

  @Override
  protected String metricPath() {
    return CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(exposureCommand(fund(GLOBAL_EQUITY_FUND, 50_000), etf(WORLD_ETF, 50_000)));
  }

  /**
   * A portfolio shaped like a client's rather than the minimum the shared scenario needs: two equity funds, a world ETF
   * and an individual stock — the equity-bearing holdings — alongside an individual bond, cash and a GIC, which this
   * metric scopes out of both the breakdown and its denominator.
   */
  @Override
  protected String requestBodyForPositiveMicScenario() {
    return writeJson(exposureCommand(
        fund(GLOBAL_EQUITY_FUND, 40_000),
        fund(CANADIAN_EQUITY_FUND, 20_000),
        etf(WORLD_ETF, 20_000),
        stock(STOCK_TICKER, STOCK_EXCHANGE, 20_000),
        bond("CA135087P493", 50_000),
        cash("CASH-CAD", Currency.CAD, 30_000),
        gic("GIC-RBC-3Y", Currency.CAD, 20_000, 1095)));
  }

  @Override
  protected String micPositiveResponseBody() {
    return writeJson(positiveScenarioRows());
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds seven
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
    command.setHoldings(List.of(fund(GLOBAL_EQUITY_FUND, 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload, derived by hand from the request above. Only the four equity-bearing holdings form the
   * denominator — 100 000 of the portfolio's 200 000 — so they weigh 0.4 / 0.2 / 0.2 / 0.2. US is 0.4·0.50 = 0.20;
   * CANADA is 0.4·0.30 + 0.2·1.00 + 0.2 (the stock's whole weight, from its business country) = 0.52; EUROPE is
   * 0.4·0.20 + 0.2·0.40 = 0.16; ASIA is 0.2·0.60 = 0.12.
   *
   * <p>
   * The distribution already totals 100% before any rescaling, which is what makes the sleeve boundary visible here:
   * had the bond, the cash or the GIC entered the denominator, each bucket would come back at half its value and the
   * total would still be 100% after the rescale — a defect no total-only assertion could catch.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    EquityGeographicExposureResult result = readJson(responseBody, EquityGeographicExposureResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertDistribution(result.getGeographicExposure(), GeographicRegionType.class, Map.of(
        GeographicRegionType.US, "0.20",
        GeographicRegionType.CANADA, "0.52",
        GeographicRegionType.EUROPE, "0.16",
        GeographicRegionType.ASIA, "0.12"));
    assertTotalsToOne(result.getGeographicExposure());
  }

  /**
   * The two gaps this metric distinguishes, one per branch: a fund whose row carries no regions
   * ({@code MISSING_EQUITY_GEOGRAPHIC_EXPOSURE}) and a stock Market Investment Catalogue has no geography for at all
   * ({@code SECURITY_NOT_FOUND_FOR_METRIC}). Both keep their weight in UNKNOWN rather than leaving the distribution, so
   * the client can see how much of the equity sleeve the breakdown does not describe — here exactly half of it.
   */
  @Test
  void shouldWarnAndBucketUnknown_whenRegionsAreMissing() {
    PortfolioHolding allocated = fund(GLOBAL_EQUITY_FUND, 50_000);
    PortfolioHolding withoutRegions = fund(CANADIAN_EQUITY_FUND, 25_000);
    PortfolioHolding stockWithoutGeography = stock(STOCK_TICKER, STOCK_EXCHANGE, 25_000);
    micMockServer.setDispatcher(compositeDispatcher(Map.of(
        CompositeSecurityAttribute.EQUITY_GEOGRAPHIC_ALLOCATION, List.of(
            geographicAllocationRow(GLOBAL_EQUITY_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.US, "1.00")),
            emptyGeographicAllocationRow(CANADIAN_EQUITY_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD)),
        CompositeSecurityAttribute.GEOGRAPHY, List.of())));

    var response = postCalculation(writeJson(
        exposureCommand(allocated, withoutRegions, stockWithoutGeography)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    EquityGeographicExposureResult result = readJson(response.responseBody(), EquityGeographicExposureResult.class);
    assertThat(result.getWarnings()).hasSize(2);
    assertMissingRegionsWarning(result.getWarnings().getFirst(), withoutRegions);
    assertSecurityNotFoundWarning(result.getWarnings().get(1), stockWithoutGeography);
    assertDistribution(result.getGeographicExposure(), GeographicRegionType.class, Map.of(
        GeographicRegionType.US, "0.50",
        GeographicRegionType.UNKNOWN, "0.50"));
    assertTotalsToOne(result.getGeographicExposure());
  }

  private static void assertMissingRegionsWarning(Notification warning, PortfolioHolding expectedFor) {
    String holdingId = expectedFor.getIdsString();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE);
    assertThat(warning.getMessage())
        .isEqualTo(ErrorCode.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE.getFormattedMessage(holdingId));
    assertThat(warning.getDescription()).isEqualTo(ErrorCode.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE.getDescription());
    assertThat(warning.getAction()).isEqualTo(ErrorCode.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE.getAction());
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holdingId)
        .containsEntry("param-1", holdingId);
  }

  /**
   * Asserts the whole notification: this one substitutes the metric name into the message and the holding id into the
   * metadata only, so a regression that swaps the two arguments produces a message naming a holding where the metric
   * belongs — which a code-only assertion would pass.
   */
  private static void assertSecurityNotFoundWarning(Notification warning, PortfolioHolding expectedFor) {
    String holdingId = expectedFor.getIdsString();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertThat(warning.getMessage()).isEqualTo(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC
        .getFormattedMessage(holdingId, CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE.getUserFriendlyName()));
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holdingId)
        .containsEntry("param-2", CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE.getUserFriendlyName());
  }

  private static Map<CompositeSecurityAttribute, List<? extends SecurityAttributeResult<?>>> positiveScenarioRows() {
    return Map.of(
        CompositeSecurityAttribute.EQUITY_GEOGRAPHIC_ALLOCATION, List.of(
            geographicAllocationRow(GLOBAL_EQUITY_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.US, "0.50"),
                regionValue(GeographicRegionType.CANADA, "0.30"),
                regionValue(GeographicRegionType.EUROPE, "0.20")),
            geographicAllocationRow(CANADIAN_EQUITY_FUND, FiIdentifierType.MORNINGSTAR_ID, Currency.CAD,
                regionValue(GeographicRegionType.CANADA, "1.00")),
            geographicAllocationRow(WORLD_ETF, FiIdentifierType.TICKER, Currency.CAD,
                regionValue(GeographicRegionType.ASIA, "0.60"),
                regionValue(GeographicRegionType.EUROPE, "0.40"))),
        CompositeSecurityAttribute.GEOGRAPHY, List.of(
            geographyRow(GLOBAL_EQUITY_FUND, FiIdentifierType.MORNINGSTAR_ID, null, null, Currency.CAD),
            geographyRow(CANADIAN_EQUITY_FUND, FiIdentifierType.MORNINGSTAR_ID, null, null, Currency.CAD),
            geographyRow(WORLD_ETF, FiIdentifierType.TICKER, null, null, Currency.CAD),
            geographyRow(STOCK_TICKER, FiIdentifierType.TICKER_MIC, Country.CANADA, null, Currency.CAD)));
  }

  private static PortfolioHoldingsCommand exposureCommand(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE)
        .holdings(List.of(holdings))
        .dataProviders(morningstarOnly())
        .build();
  }
}
