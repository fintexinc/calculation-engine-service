package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.BusinessCountryCacheStorage;
import com.fintex.ce.adapter.cache.EquityGeographicAllocationCacheStorage;
import com.fintex.ce.domain.enumeration.Country;
import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.service.GeographicAllocationMappingService;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_RRC_EGE_001;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityGeographicAllocationCacheStorageTest {

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));

      final List<Holding> holdings = List.of(new Holding());
      final List<FundSeriesHolding> filtered = List.of(new FundSeriesHolding().setFundServCode("TEST"));

      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_RRC_EGE_001)))
          .thenReturn(Map.of());
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(sut).loadBenchOfFundCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadCanadaPooledFunds() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));
      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_RRC_EGE_001)))
          .thenReturn(Map.of());

      final List<Holding> holdings = List.of(new Holding());
      final List<CanadaPooledFundHolding> filtered = List.of(new CanadaPooledFundHolding().setMorningstarId("TEST"));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_POOLED_FUND_PREDICATE)))
          .thenReturn(filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(sut).loadCanadaPooledFunds(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadUsMutualFunds() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));
      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_RRC_EGE_001)))
          .thenReturn(Map.of());

      final List<Holding> holdings = List.of(new Holding());
      final List<UsMutualFundHolding> filtered = List.of(new UsMutualFundHolding().setTicker("TEST"));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_MUTUAL_FUND_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(sut).loadUsMutualFunds(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadCanadaHedgeFunds() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));
      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_RRC_EGE_001)))
          .thenReturn(Map.of());

      final List<Holding> holdings = List.of(new Holding());
      final List<CanadaHedgeFundHolding> filtered = List.of(new CanadaHedgeFundHolding().setMorningstarId("TEST"));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_HEDGE_FUND_PREDICATE)))
          .thenReturn(filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(sut).loadCanadaHedgeFunds(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));
      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_RRC_EGE_001)))
          .thenReturn(Map.of());

      final List<Holding> holdings = List.of(new Holding());
      final List<EtfHolding> filtered = List.of(new EtfHolding().setTicker("TEST").setExchangeCode("TST"));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(sut).loadForBenchOfEtfUs(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));
      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_RRC_EGE_001)))
          .thenReturn(Map.of());

      final List<Holding> holdings = List.of(new Holding());
      final List<EtfHolding> filtered = List.of(new EtfHolding().setTicker("TEST").setExchangeCode("TST"));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(sut).loadForBenchOfEtfCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfBenchmarks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));

      final List<Holding> holdings = List.of(new Holding());
      final List<BenchmarkIndexHolding> filtered = List.of(new BenchmarkIndexHolding().setMrStarId("TEST"));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(sut).loadForBenchOfBenchmarks(filtered, List.of());
    }
  }

  @Test
  void load_mapForNoneStock() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));

      final List<Holding> holdings = List.of(new Holding());
      final Map allocations = mock(Map.class);
      final Map nonStockResult = mock(Map.class);
      final FundSeriesHolding fundSeries = new FundSeriesHolding().setFundServCode("TEST");
      final EquityCountryAllocation equityCountryAllocation = mock(EquityCountryAllocation.class);
      final List<FundSeriesHolding> filtered = List.of(fundSeries);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)))
          .thenReturn(filtered);
      when(sut.loadBenchOfFundCanada(filtered, List.of()))
          .thenReturn(Map.of(fundSeries, equityCountryAllocation));
      when(equityCountryAllocation.getAllocations()).thenReturn(allocations);
      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_RRC_EGE_001)))
          .thenReturn(Map.of(fundSeries, nonStockResult));

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      final Map<Holding, Map<GeographicRegionType, BigDecimal>> result = sut.load(
          holdings,
          List.of(),
          warnings,
          new ParamHolderDTO());

      // VERIFY
      Assertions.assertEquals(1, result.size());
      final var entry = result.entrySet().stream().findFirst().orElseThrow();
      Assertions.assertEquals(fundSeries, entry.getKey());
      Assertions.assertEquals(nonStockResult, entry.getValue());
    }
  }

  @Test
  void load_mapForStock() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);
      final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

      final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
          withSettings().useConstructor(null, null, null, geographicAllocationMappingService, businessCountryCacheStorage));

      final List<Holding> holdings = List.of(new Holding());
      final Map allocations = mock(Map.class);
      final Map nonStockResult = mock(Map.class);
      final StockHolding stockHolding = new StockHolding().setTicker("TEST").setExchangeCode("TST");
      final EquityCountryAllocation equityCountryAllocation = mock(EquityCountryAllocation.class);
      final List<StockHolding> filtered = List.of(stockHolding);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)))
          .thenReturn(filtered);
      when(sut.loadForBenchOfStock(filtered, List.of()))
          .thenReturn(Map.of(stockHolding, equityCountryAllocation));
      when(equityCountryAllocation.getAllocations()).thenReturn(allocations);
      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_RRC_EGE_001)))
          .thenReturn(Map.of(stockHolding, nonStockResult));
      when(businessCountryCacheStorage.loadBusinessCountries(any(), any(), anyBoolean(), anyList()))
          .thenReturn(Map.of(stockHolding, Country.CAN));

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      final Map<Holding, Map<GeographicRegionType, BigDecimal>> result = sut.load(
          holdings,
          List.of(),
          warnings,
          new ParamHolderDTO());

      // VERIFY
      Assertions.assertEquals(1, result.size());
      final var entry = result.entrySet().stream().findFirst().orElseThrow();
      Assertions.assertEquals(stockHolding, entry.getKey());
      Assertions.assertEquals(1, entry.getValue().size());
      final Map.Entry<GeographicRegionType, BigDecimal> regionTypeResult = entry.getValue().entrySet()
          .stream().findFirst().orElseThrow();
      Assertions.assertEquals(GeographicRegionType.CANADA, regionTypeResult.getKey());
      Assertions.assertEquals(BigDecimal.ONE, regionTypeResult.getValue());
    }
  }

}