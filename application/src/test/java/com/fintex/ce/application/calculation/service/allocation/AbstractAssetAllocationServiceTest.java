package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationData;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.RegionDatapoint;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Shared assertions for asset-allocation breakdown services. Mirrors the service hierarchy: every behaviour driven by
 * {@link AbstractAssetAllocationService} is exercised once here, and the concrete tests only declare the per-service
 * expected distributions (where the regular AA collapses {@code EM_EQUITIES} into {@code INTERNATIONAL_EQUITIES} and
 * the EM variant keeps them separate). Each test asserts the full per-region distribution via
 * {@link #assertAllocationEquals} — every emitted bucket is checked, not just the headline non-zero ones.
 */
abstract class AbstractAssetAllocationServiceTest<R extends BaseCalculationResult> {

  protected static final BigDecimal TOLERANCE = new BigDecimal("0.0000000001");
  protected static final BigDecimal HALF = new BigDecimal("0.5");

  protected final FxRateService fxRateService = mock(FxRateService.class);
  protected final DefaultTargetCurrencyConverter currencyConverter = new DefaultTargetCurrencyConverter(
      fxRateService, new FxProperties());
  protected final PortfolioWeightCalculator portfolioWeightCalculator = new PortfolioWeightCalculator(
      currencyConverter);

  protected AbstractAssetAllocationService<R> service;

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

  protected abstract AbstractAssetAllocationService<R> createService();

  protected abstract Map<AssetAllocationRegionType, BigDecimal> getAllocation(R result);

  protected abstract List<Notification> getWarnings(R result);

  /**
   * The set of region types this service emits. The regular AA omits {@code EM_EQUITIES} (collapsed into
   * {@code INTERNATIONAL_EQUITIES}); the EM-aware variant emits the full enum.
   */
  protected abstract Set<AssetAllocationRegionType> emittedTypes();

  protected abstract Map<AssetAllocationRegionType, BigDecimal> expectedForEmergingMarketStockAlone();

  protected abstract Map<AssetAllocationRegionType, BigDecimal> expectedForCashHalfPlusEmStockHalf();

  protected abstract Map<AssetAllocationRegionType, BigDecimal> expectedForSamsungPlusEmEtf();

  protected abstract Map<AssetAllocationRegionType, BigDecimal> expectedForCalculateOnSingleEmEquity();

  protected Map<AssetAllocationRegionType, BigDecimal> baseline() {
    Map<AssetAllocationRegionType, BigDecimal> map = new EnumMap<>(AssetAllocationRegionType.class);
    emittedTypes().forEach(type -> map.put(type, ZERO));
    return map;
  }

  protected Map<AssetAllocationRegionType, BigDecimal> singleBucket(AssetAllocationRegionType type, BigDecimal value) {
    Map<AssetAllocationRegionType, BigDecimal> map = baseline();
    map.put(type, value);
    return map;
  }

  protected void assertAllocationEquals(R result, Map<AssetAllocationRegionType, BigDecimal> expected) {
    Map<AssetAllocationRegionType, BigDecimal> actual = getAllocation(result);
    assertThat(actual).containsOnlyKeys(expected.keySet());
    expected.forEach((key, expectedValue) -> assertThat(actual.get(key))
        .as("region %s", key)
        .isCloseTo(expectedValue, within(TOLERANCE)));
  }

  protected static AssetAllocationData data(Map<PortfolioHolding, HoldingAssetAllocation> fundAllocations,
      Map<PortfolioHolding, Geography> stockGeographies) {
    return new AssetAllocationData(fundAllocations, stockGeographies);
  }

  @Test
  void cashHolding_classifiedAs100PercentCash() {
    CashHolding cash = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .build();

    R result = service.perform(command(cash), data(Map.of(), Map.of()));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.CASH, ONE));
    assertThat(getWarnings(result)).isEmpty();
  }

  @Test
  void gicHolding_longTerm_classifiedAsFixedIncome() {
    GicHolding gic = GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .term(BigDecimal.valueOf(365))
        .build();

    R result = service.perform(command(gic), data(Map.of(), Map.of()));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.FIXED_INCOME, ONE));
  }

  @Test
  void gicHolding_shortTerm_classifiedAsCash() {
    GicHolding gic = GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .term(BigDecimal.valueOf(100))
        .build();

    R result = service.perform(command(gic), data(Map.of(), Map.of()));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.CASH, ONE));
  }

  @Test
  void usStock_mappedToUsEquities() {
    PortfolioHolding stock = stock("AAPL");
    Geography geography = Geography.builder().region(regionDatapoint(SecurityRegion.USA)).build();

    R result = service.perform(command(stock), data(Map.of(), Map.of(stock, geography)));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.US_EQUITIES, ONE));
    assertThat(getWarnings(result)).isEmpty();
  }

  @Test
  void canadianStock_mappedToCanadianEquities() {
    PortfolioHolding stock = stock("RY");
    Geography geography = Geography.builder().region(regionDatapoint(SecurityRegion.CANADA)).build();

    R result = service.perform(command(stock), data(Map.of(), Map.of(stock, geography)));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.CANADIAN_EQUITIES, ONE));
  }

  @Test
  void developedNonNorthAmericanStock_mappedToInternationalEquities() {
    PortfolioHolding stock = stock("SAP");
    Geography geography = Geography.builder().region(regionDatapoint(SecurityRegion.OTHER)).build();

    R result = service.perform(command(stock), data(Map.of(), Map.of(stock, geography)));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.INTERNATIONAL_EQUITIES, ONE));
  }

  @Test
  void emergingMarketStock_classifiedPerServiceConvention() {
    PortfolioHolding samsung = stock("005930");
    Geography geography = Geography.builder().region(regionDatapoint(SecurityRegion.EMERGING_MARKETS)).build();

    R result = service.perform(command(samsung), data(Map.of(), Map.of(samsung, geography)));

    assertAllocationEquals(result, expectedForEmergingMarketStockAlone());
  }

  @Test
  void stockWithoutGeography_addsWarningAndIsUnclassified() {
    PortfolioHolding stock = stock("XYZ");

    R result = service.perform(command(stock), data(Map.of(), Map.of()));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.UNCLASSIFIED, ONE));
    assertThat(getWarnings(result)).hasSize(1);
    assertThat(getWarnings(result)).first().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
      assertThat(warning.getMessage()).isEqualTo(
          "Security information not found by the data source for " + service.getMetric().getUserFriendlyName());
      assertThat(warning.getMetadata()).containsEntry("holdingId", stock.getIdsString());
    });
  }

  @Test
  void fundHolding_usesAllocationsFromSms() {
    PortfolioHolding fund = mutualFund("RBF605");
    HoldingAssetAllocation allocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(
            AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.6"),
            AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("0.4")))
        .build();

    R result = service.perform(command(fund), data(Map.of(fund, allocation), Map.of()));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.6"));
    expected.put(AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("0.4"));
    assertAllocationEquals(result, expected);
  }

  @Test
  void fundHoldingWithoutAllocations_addsWarningAndIsUnclassified() {
    PortfolioHolding fund = mutualFund("RBF605");
    HoldingAssetAllocation allocation = HoldingAssetAllocation.builder().allocations(Map.of()).build();

    R result = service.perform(command(fund), data(Map.of(fund, allocation), Map.of()));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.UNCLASSIFIED, ONE));
    assertThat(getWarnings(result)).hasSize(1);
    assertThat(getWarnings(result)).first().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.MISSING_ASSET_ALLOCATION);
      assertThat(warning.getMessage()).isEqualTo(
          "The holding " + fund.getIdsString() + " is missing values for Asset Allocation");
      assertThat(warning.getMetadata()).containsEntry("holdingId", fund.getIdsString());
    });
  }

  @Test
  void fundHoldingNotFoundBySm_addsSecurityNotFoundWarningAndIsUnclassified() {
    PortfolioHolding fund = mutualFund("RBF605");

    R result = service.perform(command(fund), data(Map.of(), Map.of()));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.UNCLASSIFIED, ONE));
    assertThat(getWarnings(result)).hasSize(1);
    assertThat(getWarnings(result)).first().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
      assertThat(warning.getMessage()).isEqualTo(
          "Security information not found by the data source for " + service.getMetric().getUserFriendlyName());
      assertThat(warning.getMetadata()).containsEntry("holdingId", fund.getIdsString());
    });
  }

  @Test
  void mixedPortfolio_cashAndUsStock_weightedAggregate() {
    CashHolding cash = CashHolding.builder()
        .value(BigDecimal.valueOf(50))
        .holdingType(FinancialInstrumentType.CASH)
        .build();
    PortfolioHolding stock = stock("AAPL").toBuilder().value(BigDecimal.valueOf(50)).build();

    Geography geography = Geography.builder().region(regionDatapoint(SecurityRegion.USA)).build();

    R result = service.perform(command(cash, stock), data(Map.of(), Map.of(stock, geography)));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.CASH, HALF);
    expected.put(AssetAllocationRegionType.US_EQUITIES, HALF);
    assertAllocationEquals(result, expected);
  }

  @Test
  void mixedPortfolio_cashAndEmergingMarketStock_weightedAggregate() {
    CashHolding cash = CashHolding.builder()
        .value(BigDecimal.valueOf(50))
        .holdingType(FinancialInstrumentType.CASH)
        .build();
    PortfolioHolding emStock = stock("BABA").toBuilder().value(BigDecimal.valueOf(50)).build();

    Geography geography = Geography.builder().region(regionDatapoint(SecurityRegion.EMERGING_MARKETS)).build();

    R result = service.perform(command(cash, emStock), data(Map.of(), Map.of(emStock, geography)));

    assertAllocationEquals(result, expectedForCashHalfPlusEmStockHalf());
  }

  @Test
  void samsungPlusEmEtf_aggregatesPerServiceConvention() {
    PortfolioHolding samsung = stock("005930").toBuilder().value(BigDecimal.valueOf(50)).build();
    PortfolioHolding emEtf = etf("CSEMAS").toBuilder().value(BigDecimal.valueOf(50)).build();

    Geography koreaGeography = Geography.builder().region(regionDatapoint(SecurityRegion.EMERGING_MARKETS)).build();

    HoldingAssetAllocation emEtfAllocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(
            AssetAllocationRegionType.EM_EQUITIES, new BigDecimal("0.95"),
            AssetAllocationRegionType.CASH, new BigDecimal("0.05")))
        .build();

    R result = service.perform(command(samsung, emEtf),
        data(Map.of(emEtf, emEtfAllocation), Map.of(samsung, koreaGeography)));

    assertAllocationEquals(result, expectedForSamsungPlusEmEtf());
  }

  @Test
  void smsNoise_inOtherBucketIsClampedToZero() {
    PortfolioHolding fund = mutualFund("RBF999");
    HoldingAssetAllocation noisyAllocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(
            AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.9999948939"),
            AssetAllocationRegionType.OTHER, new BigDecimal("-0.0000051061")))
        .build();

    R result = service.perform(command(fund), data(Map.of(fund, noisyAllocation), Map.of()));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.9999948939"));
    assertAllocationEquals(result, expected);
  }

  @Test
  void smsNoise_realAllocationsAboveThresholdSurvive() {
    PortfolioHolding fund = mutualFund("RBF999");
    HoldingAssetAllocation allocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(
            AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.95"),
            AssetAllocationRegionType.OTHER, new BigDecimal("0.0001")))
        .build();

    R result = service.perform(command(fund), data(Map.of(fund, allocation), Map.of()));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.95"));
    expected.put(AssetAllocationRegionType.OTHER, new BigDecimal("0.0001"));
    assertAllocationEquals(result, expected);
  }

  @Test
  void currencyAdjustedWeights_usdHoldingsConvertedToCadBeforeWeighting() {
    CashHolding cadCash = CashHolding.builder()
        .value(BigDecimal.valueOf(100))
        .holdingType(FinancialInstrumentType.CASH)
        .currency(Currency.CAD)
        .build();
    PortfolioHolding usStock = stock("AAPL").toBuilder().value(BigDecimal.valueOf(100)).build();

    Geography usGeography = Geography.builder()
        .region(regionDatapoint(SecurityRegion.USA))
        .currency(currencyDatapoint(Currency.USD))
        .build();
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    R result = service.perform(command(cadCash, usStock), data(Map.of(), Map.of(usStock, usGeography)));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.CASH, new BigDecimal("0.4"));
    expected.put(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("0.6"));
    assertAllocationEquals(result, expected);
    assertThat(getWarnings(result)).isEmpty();
  }

  @Test
  void missingFxRate_fallsBackToOriginalValuesAndAddsWarning() {
    CashHolding cadCash = CashHolding.builder()
        .value(BigDecimal.valueOf(100))
        .holdingType(FinancialInstrumentType.CASH)
        .currency(Currency.CAD)
        .build();
    PortfolioHolding usStock = stock("AAPL").toBuilder().value(BigDecimal.valueOf(100)).build();

    Geography usGeography = Geography.builder()
        .region(regionDatapoint(SecurityRegion.USA))
        .currency(currencyDatapoint(Currency.USD))
        .build();
    Map<Currency, BigDecimal> noRate = new HashMap<>();
    noRate.put(Currency.USD, null);
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(noRate);

    R result = service.perform(command(cadCash, usStock), data(Map.of(), Map.of(usStock, usGeography)));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.CASH, HALF);
    expected.put(AssetAllocationRegionType.US_EQUITIES, HALF);
    assertAllocationEquals(result, expected);
    assertThat(getWarnings(result)).hasSize(1);
    assertThat(getWarnings(result)).first().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.FX_RATES_UNAVAILABLE);
      assertThat(warning.getMessage()).contains("FX rates unavailable for holding " + usStock.getIdsString());
      assertThat(warning.getMetadata()).containsEntry("holdingId", usStock.getIdsString());
    });
  }

  @Test
  void calculate_directInvocationOnEmExposure_handlesPerServiceConvention() {
    PortfolioHolding holding = mock(PortfolioHolding.class);
    when(holding.getValue()).thenReturn(ONE);
    Map<PortfolioHolding, Map<AssetAllocationRegionType, BigDecimal>> exposures = Map.of(
        holding, Map.of(AssetAllocationRegionType.EM_EQUITIES, ONE));

    R result = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of(holding));

    assertAllocationEquals(result, expectedForCalculateOnSingleEmEquity());
  }

  protected static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holdings));
    return command;
  }

  protected static PortfolioHolding stock(String ticker) {
    return new PortfolioHolding(BigDecimal.ONE, FinancialInstrumentType.STOCK_US,
        EquitySecurityIdentifier.builder().id(ticker).idType(FiIdentifierType.TICKER).exchangeId("XNAS").build());
  }

  protected static PortfolioHolding etf(String ticker) {
    return new PortfolioHolding(BigDecimal.ONE, FinancialInstrumentType.ETF_US,
        EquitySecurityIdentifier.builder().id(ticker).idType(FiIdentifierType.TICKER).exchangeId("XNAS").build());
  }

  protected static PortfolioHolding mutualFund(String fundserv) {
    return new PortfolioHolding(BigDecimal.ONE, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier(fundserv, FiIdentifierType.FUNDSERV));
  }

  protected static RegionDatapoint regionDatapoint(SecurityRegion region) {
    RegionDatapoint dp = new RegionDatapoint();
    dp.setValue(region);
    return dp;
  }

  protected static CurrencyDatapoint currencyDatapoint(Currency currency) {
    CurrencyDatapoint dp = new CurrencyDatapoint();
    dp.setValue(currency);
    return dp;
  }
}
