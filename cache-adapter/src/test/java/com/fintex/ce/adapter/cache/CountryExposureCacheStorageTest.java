package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.CountryExposureCacheStorage;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.service.CountryAllocationMappingService;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_FICQ_BCE_001;
import static com.fintex.ce.util.FilterUtils.*;
import static com.fintex.ce.util.TestConstants.GREATER_THAN_YEAR;
import static com.fintex.ce.util.TestConstants.LESS_THAN_YEAR;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CountryExposureCacheStorageTest {

  CountryExposureCacheStorage countryExposureCacheStorage;
  Holding holding;
  List<Holding> holdings;
  List<Warning> warnings;
  CountryExposure countryExposure;
  CountryAllocationMappingService mappingService;

  @BeforeEach
  void setUp() {
    countryExposureCacheStorage = mock(CountryExposureCacheStorage.class);
    holding = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    holdings = List.of(holding);
    warnings = List.of(new Warning("id", "msg", "code"));
    countryExposure = mock(CountryExposure.class);
    mappingService = mock(CountryAllocationMappingService.class);

  }

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      doCallRealMethod().when(countryExposureCacheStorage).load(any(), any(), any(), any());

      // ACT
      countryExposureCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE)));
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    // SETUP
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      final var fundSeriesHolding = new FundSeriesHolding().setFundServCode("TEST");
      final List<FundSeriesHolding> filtered = List.of(fundSeriesHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);
      doCallRealMethod().when(countryExposureCacheStorage).load(any(), any(), any(), any());

      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      countryExposureCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(countryExposureCacheStorage).loadBenchOfFundCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfBenchmarks() {
    // SETUP
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      final var benchmarkHolding = new BenchmarkIndexHolding().setMrStarId("TEST");
      final List<BenchmarkIndexHolding> filtered = List.of(benchmarkHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(
          filtered);
      doCallRealMethod().when(countryExposureCacheStorage).load(any(), any(), any(), any());

      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      countryExposureCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(countryExposureCacheStorage).loadForBenchOfBenchmarks(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    // SETUP
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      var etfHolding = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etfHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);
      doCallRealMethod().when(countryExposureCacheStorage).load(any(), any(), any(), any());

      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      countryExposureCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(countryExposureCacheStorage).loadForBenchOfEtfUs(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfCanada() {
    // SETUP
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      var etfHolding = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etfHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filtered);
      doCallRealMethod().when(countryExposureCacheStorage).load(any(), any(), any(), any());

      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      countryExposureCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(countryExposureCacheStorage).loadForBenchOfEtfCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyAddGics() {
    try (final var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP

      final var gicHolding = new GicHolding();
      gicHolding.setType(HoldingType.GIC);
      final List<Holding> filtered = List.of(gicHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(GIC_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(countryExposureCacheStorage).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      countryExposureCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(countryExposureCacheStorage).addGics(filtered);
    }
  }

  @Test
  void addGics_ifGicIsLessThanAYearThanMapContainsAAA() {
    // SETUP
    final GicHolding gic = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    gic.setTerm(GREATER_THAN_YEAR);
    final List<Holding> holdings = List.of(gic);
    final HashMap<Holding, Map<CountryRegionType, BigDecimal>> expected = new HashMap<>();
    expected.put(gic, Map.of(CountryRegionType.CANADA, BigDecimal.ONE));
    doCallRealMethod().when(countryExposureCacheStorage).addGics(any());

    // ACT
    final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = countryExposureCacheStorage.addGics(holdings);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

  @Test
  void addGics_ifGicIsLessThanAYearThanMapContainsNothing() {
    // SETUP
    final GicHolding gic = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    gic.setTerm(LESS_THAN_YEAR);
    final List<Holding> holdings = List.of(gic);
    final HashMap<Holding, Map<CountryRegionType, BigDecimal>> expected = new HashMap<>();
    doCallRealMethod().when(countryExposureCacheStorage).addGics(any());

    // ACT
    final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = countryExposureCacheStorage.addGics(holdings);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

  @Test
  void load_verifyLoadBenchOfFixedIncomes() {
    // SETUP
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      final var fixedIncomeHolding = new FixedIncomeHolding().setIdentifier("TEST");
      final List<FixedIncomeHolding> filtered = List.of(fixedIncomeHolding);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE))).thenReturn(
          filtered);
      doCallRealMethod().when(countryExposureCacheStorage).load(any(), any(), any(), any());

      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      countryExposureCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(countryExposureCacheStorage).loadBenchOfFixedIncomes(filtered, List.of());
    }
  }

  @Test
  void mapper_verifyMapToCountryRegions() {
    // SETUP
    final CountryAllocationMappingService mappingService = mock(CountryAllocationMappingService.class);
    final CountryExposureCacheStorage c = mock(CountryExposureCacheStorage.class,
        withSettings().useConstructor(null, null, null, null, mappingService));
    final Map<String, BigDecimal> map = Map.of(CountryRegionType.CANADA.getRegion(), BigDecimal.ONE);
    when(countryExposure.getAllocations()).thenReturn(map);
    doCallRealMethod().when(c).mapper(any(), any());

    // ACT
    final List<Warning> warnings = new ArrayList<>();
    final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = c.mapper(Map.of(holding, countryExposure),
        warnings);

    // VERIFY
    verify(mappingService).mapToCountryRegions(Map.of(holding, map), warnings, WRN_FICQ_BCE_001);
  }

  @Test
  void mapper_checkResult() {
    // SETUP
    final CountryExposureCacheStorage c = mock(CountryExposureCacheStorage.class,
        withSettings().useConstructor(null, null, null, null, mappingService));
    final Map<String, BigDecimal> map = Map.of(CountryRegionType.CANADA.getRegion(), BigDecimal.ONE);
    final Map<Holding, Map<CountryRegionType, BigDecimal>> expected = Map.of(holding, Map.of());
    when(countryExposure.getAllocations()).thenReturn(map);
    when(mappingService.mapToCountryRegions(any(), any(), any()))
        .thenReturn(expected);
    doCallRealMethod().when(c).mapper(any(), any());

    // ACT
    final List<Warning> warnings = new ArrayList<>();
    final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = c.mapper(Map.of(holding, countryExposure),
        warnings);

    // VERIFY
    assertSame(expected, actual);
  }

}