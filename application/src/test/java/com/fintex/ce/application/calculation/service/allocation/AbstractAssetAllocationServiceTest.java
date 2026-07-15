package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Shared assertions for asset-allocation breakdown services. Every behaviour driven by
 * {@link AbstractAssetAllocationService} is exercised once here; the concrete tests only wire the service under test
 * and expose its result accessors. Regional equities are consolidated into a single
 * {@link AssetAllocationRegionType#EQUITY} bucket in the commons model, so both the regular and emerging-markets
 * services now produce the same distribution. Each test asserts the full distribution via
 * {@link #assertAllocationEquals} — every emitted bucket is checked.
 */
@SuppressWarnings("unchecked")
abstract class AbstractAssetAllocationServiceTest<R extends BaseCalculationResult> {

  protected static final BigDecimal TOLERANCE = new BigDecimal("0.0000000001");
  protected static final BigDecimal HALF = new BigDecimal("0.5");

  protected final SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher = mock(SecurityDataFetcher.class);
  protected final SecurityDataFetcher<Geography> geographyFetcher = mock(SecurityDataFetcher.class);
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
      Map<Currency, BigDecimal> identity = new HashMap<>();
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

  protected Map<AssetAllocationRegionType, BigDecimal> baseline() {
    Map<AssetAllocationRegionType, BigDecimal> map = new EnumMap<>(AssetAllocationRegionType.class);
    for (AssetAllocationRegionType type : AssetAllocationRegionType.values()) {
      map.put(type, ZERO);
    }
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

  @Test
  void cashHolding_classifiedAs100PercentCash() {
    CashHolding cash = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .build();
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(cash));

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
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(gic));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.FIXED_INCOME, ONE));
  }

  @Test
  void gicHolding_shortTerm_classifiedAsCash() {
    GicHolding gic = GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .term(BigDecimal.valueOf(100))
        .build();
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(gic));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.CASH, ONE));
  }

  @ParameterizedTest
  @EnumSource(value = SecurityRegion.class, names = {"USA", "CANADA", "OTHER", "EMERGING_MARKETS"})
  void stockWithResolvableRegion_mappedToEquity(SecurityRegion region) {
    PortfolioHolding stock = stock("AAPL");
    Geography geography = Geography.builder().region(regionDatapoint(region)).build();
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(stock, geography));
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(stock));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.EQUITY, ONE));
    assertThat(getWarnings(result)).isEmpty();
  }

  @Test
  void stockWithoutGeography_addsWarningAndIsUnclassified() {
    PortfolioHolding stock = stock("XYZ");
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(stock));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.UNCLASSIFIED, ONE));
    assertThat(getWarnings(result)).hasSize(1);
    assertThat(getWarnings(result)).first().extracting(Notification::getCode).isEqualTo("FDS-026");
  }

  @Test
  void fundHolding_usesAllocationsFromSms() {
    PortfolioHolding fund = mutualFund("RBF605");
    HoldingAssetAllocation allocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(
            AssetAllocationRegionType.EQUITY, new BigDecimal("0.6"),
            AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("0.4")))
        .build();
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(fund, allocation));
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(fund));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.EQUITY, new BigDecimal("0.6"));
    expected.put(AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("0.4"));
    assertAllocationEquals(result, expected);
  }

  @Test
  void fundHoldingWithoutAllocations_addsWarningAndIsUnclassified() {
    PortfolioHolding fund = mutualFund("RBF605");
    HoldingAssetAllocation allocation = HoldingAssetAllocation.builder().allocations(Map.of()).build();
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(fund, allocation));
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(fund));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.UNCLASSIFIED, ONE));
    assertThat(getWarnings(result)).hasSize(1);
    assertThat(getWarnings(result)).first().extracting(Notification::getCode).isEqualTo("FDS-018");
  }

  @Test
  void mixedPortfolio_cashAndStock_weightedAggregate() {
    CashHolding cash = CashHolding.builder()
        .value(BigDecimal.valueOf(50))
        .holdingType(FinancialInstrumentType.CASH)
        .build();
    PortfolioHolding stock = stock("AAPL").toBuilder().value(BigDecimal.valueOf(50)).build();

    Geography geography = Geography.builder().region(regionDatapoint(SecurityRegion.USA)).build();
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(stock, geography));
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(cash, stock));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.CASH, HALF);
    expected.put(AssetAllocationRegionType.EQUITY, HALF);
    assertAllocationEquals(result, expected);
  }

  @Test
  void stockPlusEtf_aggregatesEquityAndCash() {
    PortfolioHolding stock = stock("005930").toBuilder().value(BigDecimal.valueOf(50)).build();
    PortfolioHolding etf = etf("CSEMAS").toBuilder().value(BigDecimal.valueOf(50)).build();

    Geography geography = Geography.builder().region(regionDatapoint(SecurityRegion.EMERGING_MARKETS)).build();
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(stock, geography));

    HoldingAssetAllocation etfAllocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(
            AssetAllocationRegionType.EQUITY, new BigDecimal("0.95"),
            AssetAllocationRegionType.CASH, new BigDecimal("0.05")))
        .build();
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(etf, etfAllocation));

    R result = service.perform(command(stock, etf));

    // stock (50% weight) -> 100% EQUITY; etf (50% weight) -> 95% EQUITY + 5% CASH.
    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.EQUITY, new BigDecimal("0.975"));
    expected.put(AssetAllocationRegionType.CASH, new BigDecimal("0.025"));
    assertAllocationEquals(result, expected);
  }

  @Test
  void smsNoise_inOtherBucketIsClampedToZero() {
    PortfolioHolding fund = mutualFund("RBF999");
    HoldingAssetAllocation noisyAllocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(
            AssetAllocationRegionType.EQUITY, new BigDecimal("0.9999948939"),
            AssetAllocationRegionType.OTHER, new BigDecimal("-0.0000051061")))
        .build();
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(fund, noisyAllocation));
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(fund));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.EQUITY, new BigDecimal("0.9999948939"));
    assertAllocationEquals(result, expected);
  }

  @Test
  void smsNoise_realAllocationsAboveThresholdSurvive() {
    PortfolioHolding fund = mutualFund("RBF999");
    HoldingAssetAllocation allocation = HoldingAssetAllocation.builder()
        .allocations(Map.of(
            AssetAllocationRegionType.EQUITY, new BigDecimal("0.95"),
            AssetAllocationRegionType.OTHER, new BigDecimal("0.0001")))
        .build();
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(fund, allocation));
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    R result = service.perform(command(fund));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.EQUITY, new BigDecimal("0.95"));
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
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(usStock, usGeography));
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    R result = service.perform(command(cadCash, usStock));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.CASH, new BigDecimal("0.4"));
    expected.put(AssetAllocationRegionType.EQUITY, new BigDecimal("0.6"));
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
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(usStock, usGeography));
    when(assetAllocationFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    Map<Currency, BigDecimal> noRate = new HashMap<>();
    noRate.put(Currency.USD, null);
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(noRate);

    R result = service.perform(command(cadCash, usStock));

    Map<AssetAllocationRegionType, BigDecimal> expected = baseline();
    expected.put(AssetAllocationRegionType.CASH, HALF);
    expected.put(AssetAllocationRegionType.EQUITY, HALF);
    assertAllocationEquals(result, expected);
    assertThat(getWarnings(result)).hasSize(1);
    assertThat(getWarnings(result)).first().extracting(Notification::getCode).isEqualTo("FX-001");
  }

  @Test
  void calculate_directInvocationOnEquityExposure_aggregatesEquity() {
    PortfolioHolding holding = mock(PortfolioHolding.class);
    when(holding.getValue()).thenReturn(ONE);
    Map<PortfolioHolding, Map<AssetAllocationRegionType, BigDecimal>> exposures = Map.of(
        holding, Map.of(AssetAllocationRegionType.EQUITY, ONE));

    R result = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of(holding));

    assertAllocationEquals(result, singleBucket(AssetAllocationRegionType.EQUITY, ONE));
  }

  protected static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    PortfolioHoldingsCommand command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holdings));
    when(command.getDataProviders()).thenReturn(List.of());
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
