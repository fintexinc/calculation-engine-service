package ca.tangerine.pce.application.calculation.service.allocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.cash;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.etf;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.fundCa;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.tangerine.pce.application.calculation.service.FxRateService;
import ca.tangerine.pce.application.calculation.service.HoldingCurrencyConverter;
import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.application.config.FxProperties;
import ca.tangerine.pce.model.domain.calculation.allocation.GeographicExposureData;
import ca.tangerine.pce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.CashHolding;
import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.ConsolidatedGeographicExposureResult;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.util.PortfolioHoldingBuildHelper;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.allocation.SecurityRegion;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.error.Notification;

/**
 * Behaviour of the consolidated geographic exposure metric. Deliberately not part of the
 * {@link AbstractGeographicExposureServiceTest} hierarchy even though it now consumes the same
 * {@link GeographicExposureData}: that hierarchy's contract is built around a holding excluded for belonging to the
 * opposite sleeve, and this metric excludes no security type at all. Fixtures are shared through
 * {@link GeographicExposureFixtures}.
 *
 * <p>
 * The real {@link PortfolioWeightCalculator} and {@link HoldingCurrencyConverter} participate so weighting and FX are
 * exercised end to end; only {@link FxRateService} is stubbed, to keep rates deterministic.
 */
class GeographicExposureServiceTest extends GeographicExposureFixtures {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final HoldingCurrencyConverter currencyConverter = new HoldingCurrencyConverter(
      fxRateService, new FxProperties());
  private final PortfolioWeightCalculator portfolioWeightCalculator = new PortfolioWeightCalculator(currencyConverter);

  private GeographicExposureService service;

  @BeforeEach
  void setUp() {
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(invocation -> {
      Set<Currency> sources = invocation.getArgument(0);
      Map<Currency, BigDecimal> identity = new EnumMap<>(Currency.class);
      for (Currency c : sources) {
        identity.put(c, ONE);
      }
      return identity;
    });
    service = new GeographicExposureService(portfolioWeightCalculator, new StockGeographyRegionResolver());
  }

  @Test
  void shouldRequestWholeSecurityGeographicAllocationAndGeography_whenDeclaringRequiredAttributes() {
    assertThat(service.getMetric()).isEqualTo(CalculationMetric.GEOGRAPHIC_EXPOSURE);
    assertThat(service.getMetric().getValue()).isEqualTo("geographic-exposure");
    assertThat(service.requiredAttributes()).containsExactly(
        CompositeSecurityAttribute.GEOGRAPHIC_ALLOCATION, CompositeSecurityAttribute.GEOGRAPHY);
  }

  /**
   * The region buckets arrive already rolled up by Market Investment Catalogue and are reported unchanged. Rolling
   * {@code COUNTRY_ALLOCATION} up here instead would drop the exposure of any country label the catalog cannot resolve
   * — those rows reach the mapper with a null type — and renormalize the remaining regions over it.
   */
  @Test
  void shouldReportTheRegionBreakdownAsGiven_whenFundCarriesOne() {
    PortfolioHolding fund = fundCa("RBF605", 100);
    GeographicExposureData data = data(
        Map.of(fund, allocation(Map.of(
            GeographicRegionType.US, new BigDecimal("0.50"),
            GeographicRegionType.CANADA, new BigDecimal("0.20"),
            GeographicRegionType.EUROPE, new BigDecimal("0.25"),
            GeographicRegionType.ASIA, new BigDecimal("0.05")), Currency.CAD)),
        Map.of());

    ConsolidatedGeographicExposureResult result = service.perform(command(fund), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.50"),
        GeographicRegionType.CANADA, new BigDecimal("0.20"),
        GeographicRegionType.EUROPE, new BigDecimal("0.25"),
        GeographicRegionType.ASIA, new BigDecimal("0.05"))));
    assertThat(result.getWarnings()).isEmpty();
    assertThat(totalOf(result)).isEqualByComparingTo(ONE);
  }

  /**
   * The reason the metric exists: a balanced fund, a bond and a stock land in one distribution at their portfolio
   * weights. Summing the two per-sleeve metrics cannot produce this, because the balanced fund counts at full weight in
   * both of them.
   */
  @Test
  void shouldCombineEquityAndBondBearingHoldingsIntoOneDistribution_whenPortfolioIsMixed() {
    PortfolioHolding balancedFund = fundCa("BALANCED", 400);
    PortfolioHolding bond = holdingWithoutCountry(new SecurityIdentifier("BOND", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.FIXED_INCOME, BigDecimal.valueOf(400));
    PortfolioHolding stock = holding(new SecurityIdentifier("RY", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, 200);

    GeographicExposureData data = data(
        Map.of(
            balancedFund, allocation(Map.of(
                GeographicRegionType.US, new BigDecimal("0.75"),
                GeographicRegionType.CANADA, new BigDecimal("0.25")), Currency.CAD),
            bond, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD)),
        Map.of(stock, geography(Country.CANADA, Currency.CAD)));

    ConsolidatedGeographicExposureResult result = service.perform(command(balancedFund, bond, stock), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.30"),
        GeographicRegionType.CANADA, new BigDecimal("0.70"))));
    assertThat(result.getWarnings()).isEmpty();
    assertThat(totalOf(result)).isEqualByComparingTo(ONE);
  }

  /**
   * Cash and GIC carry no geography, so they must not appear in the breakdown, and the reported regions must still
   * total 100% — this is the behaviour that lets the geographic donut stand alone next to the Asset Mix donut, which
   * shows the cash slice separately.
   */
  @Test
  void shouldExcludeCashAndGicAndStillTotalOneHundredPercent_whenPortfolioHoldsThem() {
    PortfolioHolding fund = fundCa("RBF605", 250);
    CashHolding cash = cash(Currency.CAD, 500);
    GicHolding gic = PortfolioHoldingBuildHelper.gic(
        new SecurityIdentifier("GIC-" + Currency.CAD, FiIdentifierType.MORNINGSTAR_ID), Currency.CAD,
        BigDecimal.valueOf(250), BigDecimal.valueOf(365));

    GeographicExposureData data = data(
        Map.of(fund, allocation(Map.of(
            GeographicRegionType.US, new BigDecimal("0.60"),
            GeographicRegionType.CANADA, new BigDecimal("0.40")), Currency.CAD)),
        Map.of());

    ConsolidatedGeographicExposureResult result = service.perform(command(fund, cash, gic), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.60"),
        GeographicRegionType.CANADA, new BigDecimal("0.40"))));
    assertThat(result.getWarnings()).isEmpty();
    assertThat(totalOf(result)).isEqualByComparingTo(ONE);
  }

  /**
   * The gap the two country-exposure metrics still carry: without a source currency a holding's raw value enters the
   * weight denominator unconverted. Here the USD ETF is worth 1.5x the CAD fund despite an equal nominal value, so a
   * correct implementation weights it 0.6 / 0.4 rather than 0.5 / 0.5. The currency travels with the allocation
   * attribute itself, which is why {@code GEOGRAPHIC_ALLOCATION} is paired with {@code CURRENCY} upstream.
   */
  @Test
  void shouldWeightByFxConvertedValues_whenPortfolioMixesCurrencies() {
    PortfolioHolding cadFund = fundCa("CAD-FUND", 100);
    PortfolioHolding usdEtf = etf("USD-ETF", Country.USA, 100);

    GeographicExposureData data = data(
        Map.of(
            cadFund, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD),
            usdEtf, allocation(Map.of(GeographicRegionType.US, ONE), Currency.USD)),
        Map.of());
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    ConsolidatedGeographicExposureResult result = service.perform(command(cadFund, usdEtf), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.CANADA, new BigDecimal("0.4"),
        GeographicRegionType.US, new BigDecimal("0.6"))));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldUseRawValuesAndWarn_whenFxRateIsUnavailable() {
    PortfolioHolding cadFund = fundCa("CAD-FUND", 100);
    PortfolioHolding usdEtf = etf("USD-ETF", Country.USA, 100);

    GeographicExposureData data = data(
        Map.of(
            cadFund, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD),
            usdEtf, allocation(Map.of(GeographicRegionType.US, ONE), Currency.USD)),
        Map.of());
    Map<Currency, BigDecimal> noRate = new EnumMap<>(Currency.class);
    noRate.put(Currency.USD, null);
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(noRate);

    ConsolidatedGeographicExposureResult result = service.perform(command(cadFund, usdEtf), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.CANADA, new BigDecimal("0.5"),
        GeographicRegionType.US, new BigDecimal("0.5"))));
    assertThat(result.getWarnings()).hasSize(1);
    Notification warning = result.getWarnings().getFirst();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.FX_RATES_UNAVAILABLE);
    assertThat(warning.getMessage()).isEqualTo(
        "FX rates unavailable for holding " + usdEtf.getIdsString() + ": USD -> CAD");
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", usdEtf.getIdsString())
        .containsEntry("param-2", Currency.USD)
        .containsEntry("param-3", Currency.CAD);
  }

  /**
   * The weighting denominator names cash and GIC out rather than covering the whole portfolio, so this pins that their
   * presence cannot move the reported percentages — including when they are held in another currency at a non-unit
   * rate. The equivalence holds because normalization divides by the summed net products, but an argument in a javadoc
   * does not survive the next refactor of the denominator; a test does.
   */
  @Test
  void shouldReportTheSamePercentages_whetherOrNotForeignCurrencyCashIsHeld() {
    PortfolioHolding cadFund = fundCa("CAD-FUND", 100);
    PortfolioHolding usdEtf = etf("USD-ETF", Country.USA, 100);

    GeographicExposureData data = data(
        Map.of(
            cadFund, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD),
            usdEtf, allocation(Map.of(GeographicRegionType.US, ONE), Currency.USD)),
        Map.of());
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    ConsolidatedGeographicExposureResult withoutCash = service.perform(command(cadFund, usdEtf), data);
    ConsolidatedGeographicExposureResult withCash = service.perform(
        command(cadFund, usdEtf, cash(Currency.USD, 1_000), PortfolioHoldingBuildHelper.gic(
            new SecurityIdentifier("GIC-" + Currency.USD, FiIdentifierType.MORNINGSTAR_ID), Currency.USD,
            BigDecimal.valueOf(500), BigDecimal.valueOf(365))), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.CANADA, new BigDecimal("0.4"),
        GeographicRegionType.US, new BigDecimal("0.6")));
    assertExposureEquals(withoutCash, expected);
    assertExposureEquals(withCash, expected);
    assertThat(withCash.getWarnings()).isEmpty();
  }

  /**
   * A fund whose attribute row carries no currency keeps its exposure and falls back to its raw value for weighting,
   * exactly as when an FX rate is unavailable — the breakdown degrades rather than losing the holding.
   */
  @Test
  void shouldStillReportExposureOnRawWeights_whenAllocationCarriesNoCurrency() {
    PortfolioHolding withCurrency = fundCa("WITH-CCY", 100);
    PortfolioHolding withoutCurrency = etf("NO-CCY", Country.USA, 100);

    GeographicExposureData data = data(
        Map.of(
            withCurrency, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD),
            withoutCurrency, allocation(Map.of(GeographicRegionType.US, ONE), null)),
        Map.of());
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    ConsolidatedGeographicExposureResult result = service.perform(command(withCurrency, withoutCurrency), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.CANADA, new BigDecimal("0.5"),
        GeographicRegionType.US, new BigDecimal("0.5"))));
    assertThat(result.getWarnings()).isEmpty();
    assertThat(totalOf(result)).isEqualByComparingTo(ONE);
  }

  /**
   * Market Investment Catalogue publishes no region breakdown for an individual stock, so the stock branch resolves one
   * region from the security's geography — hence {@code GEOGRAPHY} is required alongside the allocation attribute.
   */
  @Test
  void shouldAttributeStockToItsBusinessCountryRegion_whenHoldingIsSingleStock() {
    PortfolioHolding usStock = holding(new SecurityIdentifier("AAPL", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 100);
    PortfolioHolding canadaStock = holding(new SecurityIdentifier("RY", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, 100);

    GeographicExposureData data = data(Map.of(), Map.of(
        usStock, geography(Country.USA, Currency.USD),
        canadaStock, geography(Country.CANADA, Currency.CAD)));

    ConsolidatedGeographicExposureResult result = service.perform(command(usStock, canadaStock), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.5"),
        GeographicRegionType.CANADA, new BigDecimal("0.5"))));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldFallBackToSecurityRegion_whenStockHasNoBusinessCountry() {
    PortfolioHolding stock = holding(new SecurityIdentifier("NO-COUNTRY", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 100);

    GeographicExposureData data = data(Map.of(),
        Map.of(stock, geographyWithRegionOnly(SecurityRegion.EMERGING_MARKETS, Currency.USD)));

    ConsolidatedGeographicExposureResult result = service.perform(command(stock), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.OTHER, ONE)));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldReportUnknownAndWarnDistinctly_whenSecurityIsUnresolvedVersusAllocationEmpty() {
    PortfolioHolding notFoundBySm = fundCa("NOT-FOUND", 100);
    PortfolioHolding resolvedButEmpty = fundCa("EMPTY", 100);

    GeographicExposureData data = data(
        Map.of(resolvedButEmpty, allocation(Map.of(), Currency.CAD)),
        Map.of());

    ConsolidatedGeographicExposureResult result = service.perform(command(notFoundBySm, resolvedButEmpty), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.UNKNOWN, ONE)));
    assertThat(result.getWarnings()).hasSize(2);

    Notification unresolved = warningWithCode(result, ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertThat(unresolved.getMessage()).isEqualTo("Security information not found by the data source for "
        + CalculationMetric.GEOGRAPHIC_EXPOSURE.getUserFriendlyName());
    assertThat(unresolved.getMetadata()).containsEntry("holdingId", notFoundBySm.getIdsString());

    Notification empty = warningWithCode(result, ErrorCode.Codes.MISSING_GEOGRAPHIC_EXPOSURE);
    assertThat(empty.getMessage()).isEqualTo(
        "The holding " + resolvedButEmpty.getIdsString() + " is missing values for Geographic Exposure");
    assertThat(empty.getMetadata()).containsEntry("holdingId", resolvedButEmpty.getIdsString());
  }

  @Test
  void shouldReportUnknownAndWarn_whenAllocationMapIsNull() {
    PortfolioHolding fund = fundCa("NULL-ALLOCATIONS", 100);

    GeographicExposureData data = data(
        Map.of(fund, HoldingGeographicAllocation.builder().allocations(null).currency(Currency.CAD).build()),
        Map.of());

    ConsolidatedGeographicExposureResult result = service.perform(command(fund), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.UNKNOWN, ONE)));
    assertThat(result.getWarnings()).hasSize(1);
    Notification warning = result.getWarnings().getFirst();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.MISSING_GEOGRAPHIC_EXPOSURE);
    assertThat(warning.getMessage())
        .isEqualTo("The holding " + fund.getIdsString() + " is missing values for Geographic Exposure");
    assertThat(warning.getMetadata()).containsEntry("holdingId", fund.getIdsString());
  }

  /**
   * An unresolved holding keeps its weight in the UNKNOWN bucket instead of being dropped, so the missing data stays
   * visible rather than being silently absorbed by the regions that did resolve.
   */
  @Test
  void shouldKeepUnresolvedHoldingWeightInUnknown_whenOnlySomeHoldingsResolve() {
    PortfolioHolding resolved = fundCa("RESOLVED", 300);
    PortfolioHolding unresolved = fundCa("UNRESOLVED", 100);

    GeographicExposureData data = data(
        Map.of(resolved, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD)),
        Map.of());

    ConsolidatedGeographicExposureResult result = service.perform(command(resolved, unresolved), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.75"),
        GeographicRegionType.UNKNOWN, new BigDecimal("0.25"))));
    assertThat(totalOf(result)).isEqualByComparingTo(ONE);
  }

  @Test
  void shouldReturnNullBuckets_whenPortfolioIsEmptyOrHoldsOnlyCashAndGic() {
    ConsolidatedGeographicExposureResult empty = service.perform(command(), data(Map.of(), Map.of()));
    ConsolidatedGeographicExposureResult cashOnly = service.perform(
        command(cash(Currency.CAD, 1000), PortfolioHoldingBuildHelper.gic(
            new SecurityIdentifier("GIC-" + Currency.USD, FiIdentifierType.MORNINGSTAR_ID), Currency.USD,
            BigDecimal.valueOf(500), BigDecimal.valueOf(365))), data(Map.of(), Map.of()));

    assertNullExposure(empty);
    assertThat(empty.getWarnings()).isEmpty();
    assertNullExposure(cashOnly);
    assertThat(cashOnly.getWarnings()).isEmpty();
  }

  /**
   * Market Investment Catalogue rescales the breakdown to fractions summing to one, but the metric does not depend on
   * that: the inherited normalization re-bases whatever scale arrives, so a percentage-scaled row still reports the
   * same distribution.
   */
  @Test
  void shouldRescaleToOneHundredPercent_whenValuesArePercentagesRatherThanFractions() {
    PortfolioHolding fund = fundCa("PERCENT-SCALE", 100);
    GeographicExposureData data = data(
        Map.of(fund, allocation(Map.of(
            GeographicRegionType.US, new BigDecimal("80"),
            GeographicRegionType.EUROPE, new BigDecimal("20")), Currency.CAD)),
        Map.of());

    ConsolidatedGeographicExposureResult result = service.perform(command(fund), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.8"),
        GeographicRegionType.EUROPE, new BigDecimal("0.2"))));
  }

  private static Notification warningWithCode(ConsolidatedGeographicExposureResult result, String code) {
    return result.getWarnings().stream()
        .filter(w -> code.equals(w.getCode()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no warning with code " + code));
  }

  private static BigDecimal totalOf(ConsolidatedGeographicExposureResult result) {
    return result.getGeographicExposure().values().stream()
        .filter(Objects::nonNull)
        .reduce(ZERO, BigDecimal::add);
  }
}
