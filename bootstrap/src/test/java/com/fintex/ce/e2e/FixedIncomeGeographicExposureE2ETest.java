package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.e2e.BreakdownDistributions.assertDistribution;
import static com.fintex.ce.e2e.BreakdownDistributions.assertTotalsToOne;
import static com.fintex.ce.e2e.SmsAttributeResponses.compositeDispatcher;
import static com.fintex.ce.e2e.SmsAttributeResponses.emptyGeographicAllocationRow;
import static com.fintex.ce.e2e.SmsAttributeResponses.geographicAllocationRow;
import static com.fintex.ce.e2e.SmsAttributeResponses.geographyRow;
import static com.fintex.ce.e2e.SmsAttributeResponses.morningstarOnly;
import static com.fintex.ce.e2e.SmsAttributeResponses.regionValue;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.fundCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.gic;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.stockCa;
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
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(exposureCommand(fundCa(GLOBAL_BOND_FUND, 50_000), etfCa(BOND_ETF, 50_000)));
  }

  /**
   * A portfolio shaped like a client's rather than the minimum the shared scenario needs: two bond funds and a bond ETF
   * — the bond-bearing holdings — alongside an individual stock, cash and a GIC, which this metric scopes out of both
   * the breakdown and its denominator. The stock is the largest single holding in the request for exactly that reason.
   */
  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(exposureCommand(
        fundCa(GLOBAL_BOND_FUND, 40_000),
        etfCa(BOND_ETF, 20_000),
        fundCa(EUROPEAN_BOND_FUND, 20_000),
        stockCa("RY.TO", "TSX", 50_000),
        cash(Currency.CAD, 30_000),
        gic(new SecurityIdentifier("GIC-RBC-3Y", FiIdentifierType.TICKER), Currency.CAD, BigDecimal.valueOf(20_000),
            BigDecimal.valueOf(1095))));
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(positiveScenarioRows());
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds six
   * across three identifier types, and how many attribute calls the fetcher batches them into is an implementation
   * detail. Answering every {@code /attributes} call from a dispatcher keeps the test about the metric rather than
   * about the batching.
   */
  @Override
  protected void enqueueForPositiveSmsScenario() {
    smsMockServer.setDispatcher(compositeDispatcher(positiveScenarioRows()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(fundCa(GLOBAL_BOND_FUND, 50_000)));
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
    PortfolioHolding allocatedFund = fundCa(GLOBAL_BOND_FUND, 50_000);
    PortfolioHolding fundWithoutRegions = fundCa(EUROPEAN_BOND_FUND, 25_000);
    PortfolioHolding individualBond = holding(GOVERNMENT_BOND, FiIdentifierType.TICKER,
        FinancialInstrumentType.FIXED_INCOME, Country.CANADA, 25_000);
    PortfolioHolding excludedStock = stockCa("RY.TO", "TSX", 100_000);
    smsMockServer.setDispatcher(compositeDispatcher(Map.of(
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
    assertThat(result.getWarnings()).hasSize(2)
        .extracting(Notification::getCode)
        .containsExactlyInAnyOrder(ErrorCode.Codes.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE,
            ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertMissingRegionsWarning(
        warningWith(result.getWarnings(), ErrorCode.Codes.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE),
        fundWithoutRegions);
    assertSecurityNotFoundWarning(
        warningWith(result.getWarnings(), ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC), individualBond);
    assertThat(result.getWarnings())
        .as("a stock is not part of the fixed-income sleeve, so it cannot be reported as missing from it")
        .noneMatch(warning -> excludedStock.getIdsString().equals(warning.getMetadata().get("holdingId")));
    assertDistribution(result.getGeographicExposure(), GeographicRegionType.class, Map.of(
        GeographicRegionType.CANADA, "0.50",
        GeographicRegionType.UNKNOWN, "0.50"));
    assertTotalsToOne(result.getGeographicExposure());
  }

  /**
   * Picks a warning out by its code rather than by its position. The two warnings arise from different holdings for
   * different reasons, and the order the metric happens to emit them in is not part of its contract — indexing into the
   * list would make this an assertion about that order, and one that only fails on some JVM runs.
   */
  private static Notification warningWith(List<Notification> warnings, String expectedCode) {
    return warnings.stream()
        .filter(warning -> expectedCode.equals(warning.getCode()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no warning with code " + expectedCode));
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
