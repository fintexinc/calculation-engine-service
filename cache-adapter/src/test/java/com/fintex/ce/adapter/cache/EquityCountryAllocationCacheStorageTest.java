package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.BusinessCountryCacheStorage;
import com.fintex.ce.adapter.cache.EquityCountryAllocationCacheStorage;
import com.fintex.ce.domain.enumeration.Country;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.service.CountryAllocationMappingService;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.validation.DataProviderRequestHandlingValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.Country.CAN;
import static com.fintex.ce.domain.enumeration.Country.EMPTY;
import static com.fintex.ce.domain.enumeration.Country.USA;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_RRC_ECE_001;
import static com.fintex.ce.domain.enumeration.calculation.CountryRegionType.CANADA;
import static com.fintex.ce.domain.enumeration.calculation.CountryRegionType.INTERNATIONAL_DEVELOPED;
import static com.fintex.ce.domain.enumeration.calculation.CountryRegionType.UNITED_STATES;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityCountryAllocationCacheStorageTest {

  @Test
  void loadWithDataProvidersCheck_checkResult() {
    // SETUP
    final var sut = mock(EquityCountryAllocationCacheStorage.class);

    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final List<Holding> holdings = List.of(new Holding());
    final List<DataProvider> providers = mock(List.class);
    final var needToCheckDataProviders = false;
    final var expected = mock(Map.class);

    when(sut.load(anyList(), anyList(), anyList(), anyBoolean())).thenReturn(expected);

    doCallRealMethod().when(sut).loadWithDataProvidersCheck(any(), any(), any());
    // ACT
    final var actual = sut.loadWithDataProvidersCheck(holdings, providers, warnings);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void loadWithDataProvidersCheck_verifyLoad() {
    // SETUP
    final var sut = mock(EquityCountryAllocationCacheStorage.class);

    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final List<Holding> holdings = List.of(new Holding());
    final List<DataProvider> providers = mock(List.class);

    doCallRealMethod().when(sut).loadWithDataProvidersCheck(any(), any(), any());
    // ACT
    sut.loadWithDataProvidersCheck(holdings, providers, warnings);

    // VERIFY
    verify(sut).load(holdings, providers, warnings, true);
  }

  @Test
  void loadPublic_checkResult() {
    // SETUP
    final var sut = mock(EquityCountryAllocationCacheStorage.class);

    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final List<Holding> holdings = List.of(new Holding());
    final List<DataProvider> providers = mock(List.class);
    final var needToCheckDataProviders = false;
    final var expected = mock(Map.class);

    when(sut.load(anyList(), anyList(), anyList(), anyBoolean())).thenReturn(expected);

    doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), any(ParamHolderDTO.class));
    // ACT
    final var actual = sut.load(holdings, providers, warnings, new ParamHolderDTO());

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void loadPublic_verifyLoad() {
    // SETUP
    final var sut = mock(EquityCountryAllocationCacheStorage.class);

    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final List<Holding> holdings = List.of(new Holding());
    final List<DataProvider> providers = mock(List.class);

    doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), any(ParamHolderDTO.class));
    // ACT
    sut.load(holdings, providers, warnings, new ParamHolderDTO());

    // VERIFY
    verify(sut).load(holdings, providers, warnings, false);
  }

  @Test
  void load_verifyMapForNoneStock() {
    // SETUP
    final var sut = mock(EquityCountryAllocationCacheStorage.class);

    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final List<Holding> holdings = List.of(new Holding());
    final List<DataProvider> providers = mock(List.class);
    final var needToCheckDataProviders = false;
    final var mutualFunds = mock(Map.class);
    final var etfUs = mock(Map.class);
    final var etfCanada = mock(Map.class);

    when(sut.loadBenchOfFundCanada(anyList(), anyList())).thenReturn(mutualFunds);
    when(sut.loadForBenchOfEtfUs(anyList(), anyList())).thenReturn(etfUs);
    when(sut.loadForBenchOfEtfCanada(anyList(), anyList())).thenReturn(etfCanada);

    doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), anyBoolean());
    // ACT
    sut.load(holdings, providers, warnings, needToCheckDataProviders);

    // VERIFY
    verify(sut).mapForNoneStock(mutualFunds, warnings);
    verify(sut).mapForNoneStock(etfUs, warnings);
    verify(sut).mapForNoneStock(etfCanada, warnings);
  }

  @Test
  void load_verifyDataProviderCheckValidation() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      final var sut = mock(EquityCountryAllocationCacheStorage.class);

      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = true;
      final var mutualFunds = mock(Map.class);
      final var etfUs = mock(Map.class);
      final var etfCanada = mock(Map.class);
      final var mutualFundsValues = mock(Collection.class);
      final var etfUsValues = mock(Collection.class);
      final var etfCanadaValues = mock(Collection.class);

      when(mutualFunds.values()).thenReturn(mutualFundsValues);
      when(etfUs.values()).thenReturn(etfUsValues);
      when(etfCanada.values()).thenReturn(etfCanadaValues);
      when(sut.loadBenchOfFundCanada(anyList(), anyList())).thenReturn(mutualFunds);
      when(sut.loadForBenchOfEtfUs(anyList(), anyList())).thenReturn(etfUs);
      when(sut.loadForBenchOfEtfCanada(anyList(), anyList())).thenReturn(etfCanada);

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), anyBoolean());
      // ACT
      sut.load(holdings, providers, warnings, needToCheckDataProviders);

      // VERIFY
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(mutualFundsValues),
              any(), any()));
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(etfUsValues), any(),
              any()));
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(etfCanadaValues),
              any(), any()));
    }
  }

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquityCountryAllocationCacheStorage m = mock(EquityCountryAllocationCacheStorage.class);

      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = false;

      doCallRealMethod().when(m).load(anyList(), anyList(), anyList(), anyBoolean());
      // ACT
      m.load(holdings, providers, warnings, needToCheckDataProviders);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
      verify(m).mapForStocks(holdings, providers, needToCheckDataProviders, warnings);
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryAllocationCacheStorage.class);

      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = false;

      final List<FundSeriesHolding> filtered = List.of(new FundSeriesHolding().setFundServCode("TEST"));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), anyBoolean());
      // ACT
      sut.load(holdings, providers, warnings, needToCheckDataProviders);

      // VERIFY
      verify(sut).loadBenchOfFundCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryAllocationCacheStorage.class);

      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = false;

      final List<EtfHolding> filtered = List.of(new EtfHolding().setTicker("TEST").setExchangeCode("TST"));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), anyBoolean());
      // ACT
      sut.load(holdings, providers, warnings, needToCheckDataProviders);

      // VERIFY
      verify(sut).loadForBenchOfEtfUs(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquityCountryAllocationCacheStorage m = mock(EquityCountryAllocationCacheStorage.class);

      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = false;

      final List<EtfHolding> filtered = List.of(new EtfHolding().setTicker("TEST").setExchangeCode("TST"));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(anyList(), anyList(), anyList(), anyBoolean());
      // ACT
      m.load(holdings, providers, warnings, needToCheckDataProviders);

      // VERIFY
      verify(m).loadForBenchOfEtfCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfBenchmarks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquityCountryAllocationCacheStorage m = mock(EquityCountryAllocationCacheStorage.class);

      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = false;

      final List<BenchmarkIndexHolding> filtered = List.of(new BenchmarkIndexHolding().setMrStarId("TEST"));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(anyList(), anyList(), anyList(), anyBoolean());
      // ACT
      m.load(holdings, providers, warnings, needToCheckDataProviders);

      // VERIFY
      verify(m).loadForBenchOfBenchmarks(filtered, List.of());
    }
  }

  @Test
  void mapForStocks_verifyFilterHoldings() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(EquityCountryAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, businessCountryCacheStorage));

      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);

      final var needToCheckDataProviders = false;

      doCallRealMethod().when(sut).mapForStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      sut.mapForStocks(holdings, providers, needToCheckDataProviders, List.of());

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
    }
  }

  @Test
  void load_verifyMapForStocks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryAllocationCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = false;

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), anyBoolean());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      sut.load(holdings, providers, warnings, needToCheckDataProviders);

      // VERIFY
      verify(sut).mapForStocks(holdings, providers, needToCheckDataProviders, warnings);
    }
  }

  @Test
  void mapForNoneStock_verifyMapToCountryRegions() {
    // SETUP
    final CountryAllocationMappingService c = mock(CountryAllocationMappingService.class);

    final EquityCountryAllocationCacheStorage m = mock(EquityCountryAllocationCacheStorage.class,
        withSettings().useConstructor(null, null, null, c, null));

    final Holding h = new Holding();
    final EquityCountryAllocation v1 = mock(EquityCountryAllocation.class);
    final Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.TEN);
    when(v1.getAllocations()).thenReturn(allocations);
    final Map<Holding, EquityCountryAllocation> holdings = Map.of(h, v1);

    doCallRealMethod().when(m).mapForNoneStock(any(), any());
    // ACT
    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    m.mapForNoneStock(holdings, warnings);

    // VERIFY
    verify(c).mapToCountryRegions(Map.of(h, allocations), warnings, WRN_RRC_ECE_001);
  }

  @Test
  void mapForNoneStock_checkResult() {
    // SETUP
    final CountryAllocationMappingService c = mock(CountryAllocationMappingService.class);

    final EquityCountryAllocationCacheStorage m = mock(EquityCountryAllocationCacheStorage.class,
        withSettings().useConstructor(null, null, null, c, null));

    final Holding h = new Holding();
    final EquityCountryAllocation v1 = mock(EquityCountryAllocation.class);
    final Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.TEN);
    when(v1.getAllocations()).thenReturn(allocations);
    final Map<Holding, EquityCountryAllocation> holdings = Map.of(h, v1);

    final HashMap<Holding, Map<CountryRegionType, BigDecimal>> expected = new HashMap<>();
    when(c.mapToCountryRegions(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).mapForNoneStock(any(), any());
    // ACT
    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = m.mapForNoneStock(holdings, warnings);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void mapForStocks_verifyLoadBusinessCountries() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(EquityCountryAllocationCacheStorage.class, withSettings().useConstructor(null, null, null, null, businessCountryCacheStorage));

      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);

      doCallRealMethod().when(sut).mapForStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      sut.mapForStocks(holdings, dataProviders, false, warnings);

      // VERIFY
      verify(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false, warnings);
    }
  }

  @Test
  void loadStocks_checkResult1() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(EquityCountryAllocationCacheStorage.class, withSettings().useConstructor(null, null, null, null, businessCountryCacheStorage));

      final Holding holding = new Holding();
      final Map<Holding, Country> countries = Map.of(holding, CAN);
      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);
      Map<Holding, Map<CountryRegionType, BigDecimal>> expected = Map.of(holding, Map.of(CANADA, BigDecimal.ONE));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);
      doReturn(countries).when(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false,
          warnings);

      doCallRealMethod().when(sut).mapForStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = sut.mapForStocks(holdings, dataProviders, false,
          warnings);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void loadStocks_checkResult2() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(EquityCountryAllocationCacheStorage.class, withSettings().useConstructor(null, null, null, null, businessCountryCacheStorage));

      final Holding holding = new Holding();
      final Map<Holding, Country> countries = Map.of(holding, USA);
      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);
      Map<Holding, Map<CountryRegionType, BigDecimal>> expected = Map.of(holding, Map.of(UNITED_STATES,
          BigDecimal.ONE));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);
      doReturn(countries).when(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false,
          warnings);

      doCallRealMethod().when(sut).mapForStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = sut.mapForStocks(holdings, dataProviders, false,
          warnings);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void loadStocks_checkResult3() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(EquityCountryAllocationCacheStorage.class, withSettings().useConstructor(null, null, null, null, businessCountryCacheStorage));

      final Holding holding = new Holding();
      final Map<Holding, Country> countries = Map.of(holding, EMPTY);
      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);
      Map<Holding, Map<CountryRegionType, BigDecimal>> expected = Map.of();

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);
      doReturn(countries).when(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false,
          warnings);

      doCallRealMethod().when(sut).mapForStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = sut.mapForStocks(holdings, dataProviders, false,
          warnings);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void loadStocks_checkResult4() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(EquityCountryAllocationCacheStorage.class, withSettings().useConstructor(null, null, null, null, businessCountryCacheStorage));

      final Holding holding = new Holding();
      final Map<Holding, Country> countries = Map.of(holding, Country.OTHER);
      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);
      Map<Holding, Map<CountryRegionType, BigDecimal>> expected = Map.of(holding, Map.of(INTERNATIONAL_DEVELOPED,
          BigDecimal.ONE));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);
      doReturn(countries).when(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false,
          warnings);

      doCallRealMethod().when(sut).mapForStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = sut.mapForStocks(holdings, dataProviders, false,
          warnings);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void clearAssetAllocation_checkResult() {
    // SETUP
    final var sut = mock(EquityCountryAllocationCacheStorage.class);
    final var equity = mock(EquityCountryAllocation.class);

    doCallRealMethod().when(sut).clearAssetAllocation();

    // ACT
    sut.clearAssetAllocation().apply(equity, null);

    // VERIFY
    verify(equity).setAllocations(Map.of());
  }
}