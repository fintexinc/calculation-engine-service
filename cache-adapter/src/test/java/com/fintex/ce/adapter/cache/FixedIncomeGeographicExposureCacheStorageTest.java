package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.FixedIncomeGeographicExposureCacheStorage;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
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

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FICQ_BCE_001;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeGeographicExposureCacheStorageTest {

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);

      final FixedIncomeGeographicExposureCacheStorage sut = mock(FixedIncomeGeographicExposureCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var etf = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etf);

      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_FICQ_BCE_001)))
          .thenReturn(Map.of());
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
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);

      final FixedIncomeGeographicExposureCacheStorage sut = mock(FixedIncomeGeographicExposureCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var fsh = new FundSeriesHolding().setFundServCode("TEST");
      final List<FundSeriesHolding> filtered = List.of(fsh);

      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_FICQ_BCE_001)))
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
  void load_verifyLoadForBenchOfEtfCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);

      final FixedIncomeGeographicExposureCacheStorage sut = mock(FixedIncomeGeographicExposureCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var etf = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etf);

      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_FICQ_BCE_001)))
          .thenReturn(Map.of());
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

      final FixedIncomeGeographicExposureCacheStorage sut = mock(FixedIncomeGeographicExposureCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      final List<BenchmarkIndexHolding> filtered = List.of(new BenchmarkIndexHolding().setMrStarId("TEST"));

      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_FICQ_BCE_001)))
          .thenReturn(Map.of());
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
  void load_verifyLoadCanadaPooledFunds() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);

      final FixedIncomeGeographicExposureCacheStorage sut = mock(FixedIncomeGeographicExposureCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      final List<CanadaPooledFundHolding> filtered = List.of(new CanadaPooledFundHolding().setMorningstarId("TEST"));

      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_FICQ_BCE_001)))
          .thenReturn(Map.of());
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
  void load_verifyLoadCanadaHedgeFunds() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);

      final FixedIncomeGeographicExposureCacheStorage sut = mock(FixedIncomeGeographicExposureCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      final List<CanadaHedgeFundHolding> filtered = List.of(new CanadaHedgeFundHolding().setMorningstarId("TEST"));

      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_FICQ_BCE_001)))
          .thenReturn(Map.of());
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
  void load_verifyLoadUsMutualFunds() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);

      final FixedIncomeGeographicExposureCacheStorage sut = mock(FixedIncomeGeographicExposureCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      final List<UsMutualFundHolding> filtered = List.of(new UsMutualFundHolding().setTicker("TEST"));

      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_FICQ_BCE_001)))
          .thenReturn(Map.of());
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
  void load_verifyAddGics() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final GeographicAllocationMappingService geographicAllocationMappingService = mock(
          GeographicAllocationMappingService.class);

      final FixedIncomeGeographicExposureCacheStorage sut = mock(FixedIncomeGeographicExposureCacheStorage.class,
          withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService));

      final var gicHolding = new GicHolding();
      gicHolding.setTerm(new BigDecimal(400));
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      final List<GicHolding> filtered = List.of(gicHolding);
      when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(
          WRN_FICQ_BCE_001)))
          .thenReturn(Map.of());
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(GIC_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      final Map<Holding, Map<GeographicRegionType, BigDecimal>> result = sut.load(holdings, List.of(), warnings,
          new ParamHolderDTO());

      // VERIFY
      Assertions.assertEquals(1, result.size());
      final Map.Entry<Holding, Map<GeographicRegionType, BigDecimal>> entry = result.entrySet().stream().findFirst()
          .orElseThrow();
      Assertions.assertEquals(gicHolding, entry.getKey());
      Assertions.assertEquals(1, entry.getValue().size());
      final Map.Entry<GeographicRegionType, BigDecimal> regionTypeBigDecimalEntry = entry.getValue().entrySet().stream()
          .findFirst().orElseThrow();
      Assertions.assertEquals(GeographicRegionType.CANADA, regionTypeBigDecimalEntry.getKey());
      Assertions.assertEquals(BigDecimal.ONE, regionTypeBigDecimalEntry.getValue());
    }
  }

}