package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.allocation.GeographicExposureData;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.reference.CountryDatapoint;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Shared assertions for geographic exposure breakdown services. Mirrors the service hierarchy: every behaviour driven
 * by {@link AbstractGeographicExposureService} is exercised once here, and the concrete tests only declare the
 * type-specific relevant holding (a stock for the equity variant, a bond for the fixed-income variant) and the expected
 * per-region distribution for the heterogeneous portfolio that includes it. Each happy-path assertion checks every
 * {@link GeographicRegionType} bucket via {@link #assertExposureEquals} — silent zero buckets are validated too, not
 * just the headline non-zero ones. The real {@link PortfolioWeightCalculator} and {@link HoldingCurrencyConverter}
 * participate; only {@link FxRateService} is stubbed so FX scenarios are deterministic.
 */
abstract class AbstractGeographicExposureServiceTest<R extends GeographicExposureResult> {

  protected static final BigDecimal TOLERANCE = new BigDecimal("0.0000000001");

  protected final FxRateService fxRateService = mock(FxRateService.class);
  protected final HoldingCurrencyConverter currencyConverter = new HoldingCurrencyConverter(
      fxRateService, new FxProperties());
  protected final PortfolioWeightCalculator portfolioWeightCalculator = new PortfolioWeightCalculator(
      currencyConverter);

  protected AbstractGeographicExposureService<R> service;

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
    service = createService();
  }

  protected abstract AbstractGeographicExposureService<R> createService();

  protected abstract PortfolioHolding typeSpecificRelevantHolding();

  protected abstract Map<PortfolioHolding, HoldingGeographicAllocation> typeSpecificFundAllocations(
      PortfolioHolding typeSpecific);

  protected abstract Map<PortfolioHolding, Geography> typeSpecificStockGeographies(PortfolioHolding typeSpecific);

  protected abstract Map<GeographicRegionType, BigDecimal> expectedForHeterogeneousPortfolio();

  protected abstract PortfolioHolding excludedSecurityHolding();

  protected abstract String expectedMissingFundAllocationCode();

  protected static GeographicExposureData data(Map<PortfolioHolding, HoldingGeographicAllocation> fundAllocations,
      Map<PortfolioHolding, Geography> stockGeographies) {
    return new GeographicExposureData(fundAllocations, stockGeographies);
  }

  protected Map<GeographicRegionType, BigDecimal> zeroes() {
    Map<GeographicRegionType, BigDecimal> map = new EnumMap<>(GeographicRegionType.class);
    for (GeographicRegionType region : GeographicRegionType.values()) {
      map.put(region, ZERO);
    }
    return map;
  }

  protected Map<GeographicRegionType, BigDecimal> distribution(Map<GeographicRegionType, BigDecimal> values) {
    Map<GeographicRegionType, BigDecimal> map = zeroes();
    map.putAll(values);
    return map;
  }

  protected void assertExposureEquals(R result, Map<GeographicRegionType, BigDecimal> expected) {
    Map<GeographicRegionType, BigDecimal> actual = result.getGeographicExposure();
    assertThat(actual).containsOnlyKeys(GeographicRegionType.values());
    expected.forEach((region, expectedValue) -> assertThat(actual.get(region))
        .as("region %s", region)
        .isCloseTo(expectedValue, within(TOLERANCE)));
  }

  protected void assertNullExposure(R result) {
    Map<GeographicRegionType, BigDecimal> actual = result.getGeographicExposure();
    assertThat(actual).containsOnlyKeys(GeographicRegionType.values());
    assertThat(actual.values()).allSatisfy(v -> assertThat(v).isNull());
  }

  @Test
  void shouldReturnNullBuckets_whenHoldingsListIsEmpty() {
    R result = service.perform(command(), data(Map.of(), Map.of()));

    assertNullExposure(result);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldReturnNullBuckets_whenOnlyCashAndGicHoldings() {
    CashHolding cashCad = cash(Currency.CAD, 1000);
    CashHolding cashUsd = cash(Currency.USD, 500);
    GicHolding gic = gic(Currency.CAD, 500);

    R result = service.perform(command(cashCad, cashUsd, gic), data(Map.of(), Map.of()));

    assertNullExposure(result);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldReturnNullBucketsAndWarn_whenDataHasNoAllocations() {
    PortfolioHolding fund = canadaMutualFund("RBF605", 1000);

    R result = service.perform(command(fund), data(Map.of(), Map.of()));

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.UNKNOWN, ONE)));
    assertThat(result.getWarnings()).hasSize(1);
    assertThat(result.getWarnings().getFirst().getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
  }

  @Test
  void shouldReturnUnknownBucketAndWarnPerHolding_whenAllocationMapsAreEmptyPresentAndSmHasNoRecord() {
    PortfolioHolding presentButEmpty = canadaMutualFund("A", 100);
    PortfolioHolding notFoundBySm = canadaMutualFund("B", 100);
    GeographicExposureData data = data(Map.of(presentButEmpty, allocation(Map.of(), Currency.CAD)), Map.of());

    R result = service.perform(command(presentButEmpty, notFoundBySm), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.UNKNOWN, ONE)));
    assertThat(result.getWarnings()).hasSize(2);
    assertThat(result.getWarnings())
        .filteredOn(w -> ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC.equals(w.getCode())).hasSize(1);
    assertThat(result.getWarnings()).filteredOn(w -> expectedMissingFundAllocationCode().equals(w.getCode()))
        .hasSize(1);
  }

  @Test
  void shouldReturnUnknownBucketAndWarnPerHolding_whenAllAllocationMapsAreEmpty() {
    PortfolioHolding fundA = canadaMutualFund("A", 100);
    PortfolioHolding fundB = canadaMutualFund("B", 200);
    GeographicExposureData data = data(Map.of(
        fundA, allocation(Map.of(), Currency.CAD),
        fundB, allocation(Map.of(), Currency.CAD)), Map.of());

    R result = service.perform(command(fundA, fundB), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.UNKNOWN, ONE)));
    assertThat(result.getWarnings()).hasSize(2);
    assertThat(result.getWarnings()).allSatisfy(w -> assertThat(w.getCode())
        .isEqualTo(expectedMissingFundAllocationCode()));
  }

  @Test
  void shouldReturnUnknownBucketAndWarn_whenAllocationMapIsNull() {
    PortfolioHolding fund = canadaMutualFund("RBF605", 1000);
    GeographicExposureData data = data(Map.of(fund,
        HoldingGeographicAllocation.builder().allocations(null).currency(Currency.CAD).build()), Map.of());

    R result = service.perform(command(fund), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.UNKNOWN, ONE)));
    assertThat(result.getWarnings()).hasSize(1);
    assertThat(result.getWarnings().getFirst().getCode()).isEqualTo(expectedMissingFundAllocationCode());
  }

  @Test
  void shouldComputeFullDistribution_whenHeterogeneousMultiCurrencyPortfolio() {
    PortfolioHolding mutualFund = canadaMutualFund("RBF605", 100);
    PortfolioHolding usEtf = usEtf("VOO", 200);
    PortfolioHolding typeSpecific = typeSpecificRelevantHolding();
    CashHolding cash = cash(Currency.CAD, 1000);
    GicHolding gic = gic(Currency.USD, 500);

    Map<PortfolioHolding, HoldingGeographicAllocation> fundAllocations = new HashMap<>();
    fundAllocations.put(mutualFund, allocation(Map.of(
        GeographicRegionType.US, new BigDecimal("0.5"),
        GeographicRegionType.CANADA, new BigDecimal("0.5")), Currency.CAD));
    fundAllocations.put(usEtf, allocation(Map.of(
        GeographicRegionType.US, new BigDecimal("0.6"),
        GeographicRegionType.EUROPE, new BigDecimal("0.4")), Currency.USD));
    fundAllocations.putAll(typeSpecificFundAllocations(typeSpecific));

    GeographicExposureData data = data(fundAllocations, typeSpecificStockGeographies(typeSpecific));

    R result = service.perform(command(mutualFund, usEtf, typeSpecific, cash, gic), data);

    assertExposureEquals(result, expectedForHeterogeneousPortfolio());
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldConvertNonTargetCurrenciesToCad_whenComputingPortfolioWeights() {
    PortfolioHolding cadFund = canadaMutualFund("CAD-FUND", 100);
    PortfolioHolding usdEtf = usEtf("USD-ETF", 100);

    GeographicExposureData data = data(Map.of(
        cadFund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD),
        usdEtf, allocation(Map.of(GeographicRegionType.EUROPE, ONE), Currency.USD)), Map.of());
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    R result = service.perform(command(cadFund, usdEtf), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.4"),
        GeographicRegionType.EUROPE, new BigDecimal("0.6")));
    assertExposureEquals(result, expected);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldUseRawValuesAndAddWarning_whenFxRateUnavailable() {
    PortfolioHolding cadFund = canadaMutualFund("CAD-FUND", 100);
    PortfolioHolding usdEtf = usEtf("USD-ETF", 100);

    GeographicExposureData data = data(Map.of(
        cadFund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD),
        usdEtf, allocation(Map.of(GeographicRegionType.EUROPE, ONE), Currency.USD)), Map.of());
    Map<Currency, BigDecimal> noRate = new EnumMap<>(Currency.class);
    noRate.put(Currency.USD, null);
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(noRate);

    R result = service.perform(command(cadFund, usdEtf), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.5"),
        GeographicRegionType.EUROPE, new BigDecimal("0.5")));
    assertExposureEquals(result, expected);
    assertThat(result.getWarnings()).hasSize(1);
    assertThat(result.getWarnings()).first().extracting(Notification::getCode)
        .isEqualTo(ErrorCode.Codes.FX_RATES_UNAVAILABLE);
  }

  @Test
  void shouldFallBackToRawValue_whenAllocationCurrencyIsNull() {
    PortfolioHolding fundA = canadaMutualFund("A", 100);
    PortfolioHolding fundB = canadaMutualFund("B", 100);

    GeographicExposureData data = data(Map.of(
        fundA, allocation(Map.of(GeographicRegionType.US, ONE), null),
        fundB, allocation(Map.of(GeographicRegionType.EUROPE, ONE), Currency.USD)), Map.of());
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.USD, new BigDecimal("3")));

    R result = service.perform(command(fundA, fundB), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.25"),
        GeographicRegionType.EUROPE, new BigDecimal("0.75")));
    assertExposureEquals(result, expected);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldExcludeCashAndGic_fromExposureAggregation() {
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);
    CashHolding cashCad = cash(Currency.CAD, 500);
    CashHolding cashUsd = cash(Currency.USD, 250);
    GicHolding gic = gic(Currency.CAD, 200);

    GeographicExposureData data = data(Map.of(
        fund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD),
        cashCad, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD),
        cashUsd, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.USD),
        gic, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD)), Map.of());

    R result = service.perform(command(fund, cashCad, cashUsd, gic), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.US, ONE)));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldExcludeSecurityRelevantToOtherService_fromExposureAggregation() {
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);
    PortfolioHolding excluded = excludedSecurityHolding();

    GeographicExposureData data = data(Map.of(
        fund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD),
        excluded, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD)),
        Map.of(excluded, geography(Country.CANADA, Currency.CAD)));

    R result = service.perform(command(fund, excluded), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.US, ONE)));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldIncludeCanadaEtf_inFundAllocationPath_notStockGeographyPath() {
    PortfolioHolding canadaEtf = canadaEtf("XIU", 100);
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);

    GeographicExposureData data = data(Map.of(
        canadaEtf, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD),
        fund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD)), Map.of());

    R result = service.perform(command(canadaEtf, fund), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.CANADA, new BigDecimal("0.5"),
        GeographicRegionType.US, new BigDecimal("0.5"))));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldRescaleToSignedNetTotal_whenAllocationsContainLongAndShortValues() {
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);
    GeographicExposureData data = data(Map.of(
        fund, allocation(Map.of(
            GeographicRegionType.US, new BigDecimal("0.100"),
            GeographicRegionType.CANADA, new BigDecimal("-0.099")), Currency.CAD)), Map.of());

    R result = service.perform(command(fund), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("100"),
        GeographicRegionType.CANADA, new BigDecimal("-99")));
    assertExposureEquals(result, expected);
    assertThat(result.getGeographicExposure().values().stream()
        .filter(Objects::nonNull)
        .reduce(ZERO, BigDecimal::add)).isEqualByComparingTo(ONE);
  }

  @Test
  void shouldRescaleAllocations_whenRawAllocationsDoNotSumToOne() {
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);
    GeographicExposureData data = data(Map.of(
        fund, allocation(Map.of(
            GeographicRegionType.US, new BigDecimal("80"),
            GeographicRegionType.EUROPE, new BigDecimal("20")), Currency.CAD)), Map.of());

    R result = service.perform(command(fund), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.8"),
        GeographicRegionType.EUROPE, new BigDecimal("0.2")));
    assertExposureEquals(result, expected);
  }

  @Test
  void shouldAggregateExposuresAcrossEveryRegion_whenAllRegionsRepresented() {
    PortfolioHolding fundA = canadaMutualFund("A", 100);
    PortfolioHolding fundB = canadaMutualFund("B", 100);

    GeographicExposureData data = data(Map.of(
        fundA, allocation(Map.of(
            GeographicRegionType.US, new BigDecimal("0.4"),
            GeographicRegionType.CANADA, new BigDecimal("0.2"),
            GeographicRegionType.EUROPE, new BigDecimal("0.2"),
            GeographicRegionType.ASIA, new BigDecimal("0.2")), Currency.CAD),
        fundB, allocation(Map.of(
            GeographicRegionType.LATIN_AMERICA, new BigDecimal("0.3"),
            GeographicRegionType.AFRICA, new BigDecimal("0.3"),
            GeographicRegionType.OTHER, new BigDecimal("0.4")), Currency.CAD)), Map.of());

    R result = service.perform(command(fundA, fundB), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.2"),
        GeographicRegionType.CANADA, new BigDecimal("0.1"),
        GeographicRegionType.EUROPE, new BigDecimal("0.1"),
        GeographicRegionType.ASIA, new BigDecimal("0.1"),
        GeographicRegionType.LATIN_AMERICA, new BigDecimal("0.15"),
        GeographicRegionType.AFRICA, new BigDecimal("0.15"),
        GeographicRegionType.OTHER, new BigDecimal("0.2")));
    assertExposureEquals(result, expected);
  }

  protected static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    PortfolioHoldingsCommand cmd = mock(PortfolioHoldingsCommand.class);
    when(cmd.getHoldings()).thenReturn(List.of(holdings));
    return cmd;
  }

  protected static HoldingGeographicAllocation allocation(Map<GeographicRegionType, BigDecimal> values,
      Currency currency) {
    return HoldingGeographicAllocation.builder()
        .allocations(values.isEmpty() ? new EnumMap<>(GeographicRegionType.class) : new EnumMap<>(values))
        .currency(currency)
        .build();
  }

  protected static Geography geography(Country businessCountry, Currency currency) {
    Geography geography = new Geography();
    if (businessCountry != null) {
      geography.setBusinessCountry(new CountryDatapoint(businessCountry));
    }
    if (currency != null) {
      CurrencyDatapoint datapoint = new CurrencyDatapoint();
      datapoint.setValue(currency);
      geography.setCurrency(datapoint);
    }
    return geography;
  }

  protected static PortfolioHolding canadaMutualFund(String id, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.MUTUAL_FUND).country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.MORNINGSTAR_ID))
        .build();
  }

  protected static PortfolioHolding usEtf(String ticker, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.ETF).country(Country.USA)
        .securityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER))
        .build();
  }

  protected static PortfolioHolding canadaEtf(String ticker, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.ETF).country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER))
        .build();
  }

  protected static PortfolioHolding usStock(String ticker, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.STOCK).country(Country.USA)
        .securityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER))
        .build();
  }

  protected static PortfolioHolding canadaStock(String ticker, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.STOCK).country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER))
        .build();
  }

  protected static PortfolioHolding fixedIncome(String id, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.FIXED_INCOME)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.MORNINGSTAR_ID))
        .build();
  }

  protected static CashHolding cash(Currency currency, long value) {
    return CashHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.CASH)
        .securityIdentifier(new SecurityIdentifier("CASH-" + currency, FiIdentifierType.MORNINGSTAR_ID))
        .currency(currency)
        .build();
  }

  protected static GicHolding gic(Currency currency, long value) {
    return GicHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.GIC)
        .securityIdentifier(new SecurityIdentifier("GIC-" + currency, FiIdentifierType.MORNINGSTAR_ID))
        .currency(currency)
        .term(BigDecimal.valueOf(365))
        .build();
  }
}
