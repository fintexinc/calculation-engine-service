package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.Country;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.Country.CAN;
import static com.fintex.ce.domain.enumeration.Country.EMPTY;
import static com.fintex.ce.domain.enumeration.Country.USA;
import static com.fintex.ce.domain.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion.CANADIAN_EQUITIES;
import static com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion.INTERNATIONAL_EQUITIES;
import static com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion.UNCLASSIFIED;
import static com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion.US_EQUITIES;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

class AssetAllocationCacheStorageTest {

  @Test
  void load_checkResult() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AssetAllocationCacheStorage.class);

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final var holdings = List.of(holding);
      final var providers = List.of(EAGLE);
      final var expected = new AssetAllocationDataDTO();

      final var etfUs = mock(Map.class);
      final var canadaPooledFunds = mock(Map.class);
      final var etfCanada = mock(Map.class);
      final var fundCanada = mock(Map.class);
      final var benchmarks = mock(Map.class);
      final var stocks = mock(Map.class);
      final var canadaHedgeFundsFdsResponse = mock(Map.class);
      final var fixedIncomeFdsResponse = mock(Map.class);
      final var separatelyManagedAccountFdsResponse = mock(Map.class);
      final var usFundsFdsResponse = mock(Map.class);
      final var warnings = mock(List.class);

      expected.setEtfUsFdsResponse(etfUs);
      expected.setCanadaPooledFundFdsResponse(canadaPooledFunds);
      expected.setEtfCanadaFdsResponse(etfCanada);
      expected.setMutualFundFdsResponse(fundCanada);
      expected.setBenchmarkIndexFdsResponse(benchmarks);
      expected.setStocksFdsResponse(stocks);
      expected.setCanadaHedgeFundsFdsResponse(canadaHedgeFundsFdsResponse);
      expected.setUsFundsFdsResponse(usFundsFdsResponse);
      expected.setFixedIncomeFdsResponse(fixedIncomeFdsResponse);
      expected.setSeparatelyManagedAccountFdsResponse(separatelyManagedAccountFdsResponse);

      expected.setHoldings(holdings);

      when(sut.loadForBenchOfEtfUs(anyList(), anyList())).thenReturn(etfUs);
      when(sut.loadForBenchOfEtfCanada(anyList(), anyList())).thenReturn(etfCanada);
      when(sut.loadBenchOfFundCanada(anyList(), anyList())).thenReturn(fundCanada);
      when(sut.loadForBenchOfBenchmarks(anyList(), anyList())).thenReturn(benchmarks);
      when(sut.loadStocks(anyList(), anyList(), anyBoolean(), anyList())).thenReturn(stocks);
      when(sut.loadCanadaPooledFunds(anyList(), anyList())).thenReturn(canadaPooledFunds);
      when(sut.loadCanadaHedgeFunds(anyList(), anyList())).thenReturn(canadaHedgeFundsFdsResponse);
      when(sut.loadUsMutualFunds(anyList(), anyList())).thenReturn(usFundsFdsResponse);
      when(sut.loadBenchOfFixedIncomes(anyList(), anyList())).thenReturn(fixedIncomeFdsResponse);
      when(sut.loadBenchOfSeparatelyManagedAccounts(anyList(), anyList())).thenReturn(
          separatelyManagedAccountFdsResponse);

      doCallRealMethod().when(sut).load(any(), any(), anyBoolean(), anyList());
      // ACT
      final var actual = sut.load(holdings, providers, false, warnings);

      // VERIFY
      Assertions.assertEquals(expected, actual);
    }
  }

  @Test
  void load_verifyLoadStocks() {
    // SETUP
    final var sut = mock(AssetAllocationCacheStorage.class);

    final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final List<Holding> holdings = List.of(holding);
    final List<DataProvider> providers = List.of(EAGLE);
    final var needToCheckDataProviders = false;
    final var warnings = mock(List.class);

    doCallRealMethod().when(sut).load(any(), any(), anyBoolean(), anyList());
    // ACT
    sut.load(holdings, providers, needToCheckDataProviders, warnings);

    // VERIFY
    verify(sut).loadStocks(holdings, providers, needToCheckDataProviders, warnings);
  }

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AssetAllocationCacheStorage.class);

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final var holdings = List.of(holding);
      final var providers = mock(List.class);
      final var needToCheckDataProviders = false;
      final var warnings = mock(List.class);

      doCallRealMethod().when(sut).load(any(), any(), anyBoolean(), anyList());
      // ACT
      sut.load(holdings, providers, needToCheckDataProviders, warnings);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE)));
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AssetAllocationCacheStorage.class);

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final List<DataProvider> providers = List.of(EAGLE);
      final var needToCheckDataProviders = false;
      final var fundSeriesHolding = new FundSeriesHolding().setFundServCode("TEST");
      final List<FundSeriesHolding> filtered = List.of(fundSeriesHolding);
      final var warnings = mock(List.class);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), anyBoolean(), anyList());
      // ACT
      sut.load(holdings, providers, needToCheckDataProviders, warnings);

      // VERIFY
      verify(sut).loadBenchOfFundCanada(filtered, providers);
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AssetAllocationCacheStorage.class);

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final List<DataProvider> providers = List.of(EAGLE);
      final var needToCheckDataProviders = false;
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      var etfHolding = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etfHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(sut).load(any(), any(), anyBoolean(), anyList());
      // ACT
      sut.load(holdings, providers, needToCheckDataProviders, warnings);

      // VERIFY
      verify(sut).loadForBenchOfEtfUs(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AssetAllocationCacheStorage.class);

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final List<DataProvider> providers = List.of(EAGLE);
      final var needToCheckDataProviders = false;
      var etfHolding = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etfHolding);
      final var warnings = mock(List.class);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), anyBoolean(), anyList());
      // ACT
      sut.load(holdings, providers, needToCheckDataProviders, warnings);

      // VERIFY
      verify(sut).loadForBenchOfEtfCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfBenchmarks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AssetAllocationCacheStorage.class);

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final List<Holding> holdings = List.of(holding);
      final List<DataProvider> providers = List.of(EAGLE);
      final var needToCheckDataProviders = false;
      final var warnings = mock(List.class);

      final var benchmarkHolding = new BenchmarkIndexHolding().setMrStarId("TEST");
      final List<BenchmarkIndexHolding> filtered = List.of(benchmarkHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), anyBoolean(), anyList());
      // ACT
      sut.load(holdings, providers, needToCheckDataProviders, warnings);

      // VERIFY
      verify(sut).loadForBenchOfBenchmarks(filtered, providers);
    }
  }

  @Test
  void loadPublic_verifyLoad() {
    // SETUP
    final var sut = mock(AssetAllocationCacheStorage.class);

    final List<DataProvider> providers = List.of(EAGLE);
    final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final List<Holding> holdings = List.of(holding);
    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

    doCallRealMethod().when(sut).load(any(), any(), any(), any());
    // ACT
    sut.load(holdings, providers, warnings, new ParamHolderDTO());

    // VERIFY
    verify(sut).load(holdings, providers, false, warnings);
  }

  @Test
  void loadPublic_checkResult() {
    // SETUP
    final var sut = mock(AssetAllocationCacheStorage.class);

    final List<DataProvider> providers = List.of(EAGLE);
    final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final List<Holding> holdings = List.of(holding);
    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final var expected = mock(AssetAllocationDataDTO.class);

    when(sut.load(anyList(), anyList(), anyBoolean(), anyList())).thenReturn(expected);
    doCallRealMethod().when(sut).load(any(), any(), any(), any());
    // ACT
    final var actual = sut.load(holdings, providers, warnings, new ParamHolderDTO());

    // VERIFY
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void loadWithDataProvidesCheck_verifyLoad() {
    // SETUP
    final var sut = mock(AssetAllocationCacheStorage.class);

    final List<DataProvider> providers = List.of(EAGLE);
    final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final List<Holding> holdings = List.of(holding);
    final var warnings = mock(List.class);

    doCallRealMethod().when(sut).loadWithDataProvidersCheck(any(), any(), anyList());
    // ACT
    sut.loadWithDataProvidersCheck(holdings, providers, warnings);

    // VERIFY
    verify(sut).load(holdings, providers, true, warnings);
  }

  @Test
  void loadWithDataProvidersCheck_checkResult() {
    // SETUP
    final var sut = mock(AssetAllocationCacheStorage.class);

    final List<DataProvider> providers = List.of(EAGLE);
    final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final List<Holding> holdings = List.of(holding);
    final var expected = mock(AssetAllocationDataDTO.class);
    final var warnings = mock(List.class);

    when(sut.load(anyList(), anyList(), anyBoolean(), anyList())).thenReturn(expected);

    doCallRealMethod().when(sut).loadWithDataProvidersCheck(any(), any(), anyList());
    // ACT
    final var actual = sut.loadWithDataProvidersCheck(holdings, providers, warnings);

    // VERIFY
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void loadStocks_verifyFilterHoldings() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(AssetAllocationCacheStorage.class, withSettings().useConstructor(null, null, null,
          businessCountryCacheStorage, null));

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final var holdings = List.of(holding);
      final var providers = List.of(EAGLE);
      final var needToCheckDataProviders = false;
      final var warnings = mock(List.class);

      doCallRealMethod().when(sut).loadStocks(any(), any(), anyBoolean(), anyList());
      // ACT
      sut.loadStocks(holdings, providers, needToCheckDataProviders, warnings);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
    }
  }

  @Test
  void loadStocks_verifyLoadBusinessCountries() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(AssetAllocationCacheStorage.class, withSettings().useConstructor(null, null, null,
          businessCountryCacheStorage, null));

      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);

      doCallRealMethod().when(sut).loadStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      sut.loadStocks(holdings, dataProviders, false, warnings);

      // VERIFY
      verify(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false, warnings);
    }
  }

  @Test
  void loadStocks_checkResult1() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);
      final var sut = mock(AssetAllocationCacheStorage.class, withSettings().useConstructor(null, null, null,
          businessCountryCacheStorage, null));

      final var holding = new Holding().setType(HoldingType.CANADA_STOCKS).setValue(BigDecimal.ONE);
      final Map<Holding, Country> countries = Map.of(holding, CAN);
      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);
      Map<Holding, Map<AssetAllocationRegion, BigDecimal>> expected = Map.of(holding, Map.of(CANADIAN_EQUITIES,
          BigDecimal.ONE));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);
      doReturn(countries).when(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false,
          warnings);

      doCallRealMethod().when(sut).loadStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> actual = sut.loadStocks(holdings, dataProviders, false,
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
      final var sut = mock(AssetAllocationCacheStorage.class, withSettings().useConstructor(null, null, null,
          businessCountryCacheStorage, null));

      final var holding = new Holding().setType(HoldingType.US_STOCKS).setValue(BigDecimal.ONE);
      final Map<Holding, Country> countries = Map.of(holding, USA);
      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);
      Map<Holding, Map<AssetAllocationRegion, BigDecimal>> expected = Map.of(holding, Map.of(US_EQUITIES,
          BigDecimal.ONE));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);
      doReturn(countries).when(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false,
          warnings);

      doCallRealMethod().when(sut).loadStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> actual = sut.loadStocks(holdings, dataProviders, false,
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
      final var sut = mock(AssetAllocationCacheStorage.class, withSettings().useConstructor(null, null, null,
          businessCountryCacheStorage, null));

      final var holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
      final Map<Holding, Country> countries = Map.of(holding, EMPTY);
      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);
      Map<Holding, Map<AssetAllocationRegion, BigDecimal>> expected = Map.of(holding, Map.of(UNCLASSIFIED,
          BigDecimal.ONE));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);
      doReturn(countries).when(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false,
          warnings);

      doCallRealMethod().when(sut).loadStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> actual = sut.loadStocks(holdings, dataProviders, false,
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
      final var sut = mock(AssetAllocationCacheStorage.class, withSettings().useConstructor(null, null, null,
          businessCountryCacheStorage, null));

      final var holding = new Holding().setType(HoldingType.BENCHMARK_INDEX).setValue(BigDecimal.ONE);
      final Map<Holding, Country> countries = Map.of(holding, Country.OTHER);
      final List holdings = mock(List.class);
      final List dataProviders = mock(List.class);
      final List warnings = mock(List.class);
      Map<Holding, Map<AssetAllocationRegion, BigDecimal>> expected = Map.of(holding, Map.of(INTERNATIONAL_EQUITIES,
          BigDecimal.ONE));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(holdings);
      doReturn(countries).when(businessCountryCacheStorage).loadBusinessCountries(holdings, dataProviders, false,
          warnings);

      doCallRealMethod().when(sut).loadStocks(anyList(), anyList(), anyBoolean(), anyList());
      // ACT
      final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> actual = sut.loadStocks(holdings, dataProviders, false,
          warnings);

      // VERIFY
      assertEquals(expected, actual);
    }
  }
}