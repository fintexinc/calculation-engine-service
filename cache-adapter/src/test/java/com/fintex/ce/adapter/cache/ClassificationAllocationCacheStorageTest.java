package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.ClassificationAllocationCacheStorage;
import com.fintex.ce.adapter.cache.repository.ClassificationAllocationRepository;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fintex.ce.adapter.cache.ClassificationAllocationCacheStorage.DEFAULT_MAP;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_CA_CA_001;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ClassificationAllocationCacheStorageTest {

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var securityDataPort = mock(SecurityDataPort.class);
      final var classificationAllocationRepository = mock(ClassificationAllocationRepository.class);
      final var cacheStatisticService = mock(CacheStatisticService.class);

      final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class, withSettings()
          .useConstructor(securityDataPort, null, classificationAllocationRepository, cacheStatisticService));

      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      Map<Holding, ClassificationAllocation> holdingExposureMap = holdings.stream()
          .collect(Collectors.toMap(holding -> holding, holding -> mock(ClassificationAllocation.class)));

      List<Holding> holdingsFromMap = new ArrayList<>(holdingExposureMap.keySet());

      m.load(holdingsFromMap, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_MUTUAL_FUND_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE)));
      verify(m, times((6))).mapResponse(anyMap(), anyList());
      verify(m, times((4))).getCashTypeByCurrency(anyList(), any(), any(), any());
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var fsh = new FundSeriesHolding().setFundServCode("TEST");
      final List<FundSeriesHolding> filtered = List.of(fsh);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadBenchOfFundCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var etf = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etf);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfEtfUs(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      var etf = new EtfHolding().setTicker("TEST").setExchangeCode("TST");
      final List<EtfHolding> filtered = List.of(etf);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfEtfCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfStock() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      final List<StockHolding> filtered = List.of(new StockHolding().setTicker("TEST").setExchangeCode("TST"));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfStock(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadBenchOfFixedIncomes() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
      final List<Holding> holdings = List.of(new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE));
      final List<FixedIncomeHolding> filtered = List.of(new FixedIncomeHolding().setIdentifier("TEST"));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadBenchOfFixedIncomes(filtered, List.of());
    }
  }

  @Test
  void mapResponse_verifyClassificationAllocationMapper() {
    // SETUP
    final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
    final Holding h = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final ClassificationAllocation classificationAllocation = mock(ClassificationAllocation.class);
    final Map<Holding, ClassificationAllocation> holdingClassificationAllocationMap = Map.of(h,
        classificationAllocation);

    doCallRealMethod().when(m).mapResponse(any(), any());
    // ACT
    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));

    m.mapResponse(holdingClassificationAllocationMap, warnings);

    // VERIFY
    verify(m).getClassificationAllocationMapper(
        argThat(arg -> arg.getKey() == h && arg.getValue() == classificationAllocation),
        eq(warnings));
  }

  @Test
  void mapResponse_checkResult() {
    // SETUP
    final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
    final Holding h = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final ClassificationAllocation classificationAllocation = mock(ClassificationAllocation.class);
    final Map<Holding, ClassificationAllocation> holdingClassificationAllocationMap = Map.of(h,
        classificationAllocation);
    final Map<ClassificationAllocationType, BigDecimal> actualValue = Map.of(
        ClassificationAllocationType.ALTERNATIVE_INVESTMENTS__INTERNATIONAL, BigDecimal.TEN);

    when(m.getClassificationAllocationMapper(any(), any())).thenReturn(actualValue);

    doCallRealMethod().when(m).mapResponse(any(), any());
    // ACT
    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final Map<Holding, Map<ClassificationAllocationType, BigDecimal>> actual = m.mapResponse(
        holdingClassificationAllocationMap, warnings);

    // VERIFY
    assertEquals(Map.of(h, actualValue), actual);
  }

  @Test
  void classificationAllocationMapper_checkResult() {
    // SETUP
    final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
    final Holding h = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final Map.Entry<Holding, ClassificationAllocation> entry = mock(Map.Entry.class);

    when(entry.getKey()).thenReturn(h);

    final ClassificationAllocation classificationAllocation = mock(ClassificationAllocation.class);
    final Map<String, BigDecimal> emptyMap = Map.of();

    when(classificationAllocation.getSecurityClassificationValues()).thenReturn(emptyMap);

    when(entry.getValue()).thenReturn(classificationAllocation);

    doCallRealMethod().when(m).getClassificationAllocationMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    final Map<ClassificationAllocationType, BigDecimal> actual = m.getClassificationAllocationMapper(entry, warnings);

    // VERIFY
    assertEquals(DEFAULT_MAP, actual);
    assertEquals(1, warnings.size());
    assertEquals(WRN_CA_CA_001.getMessage(), warnings.get(0).getMessage());
    assertEquals(WRN_CA_CA_001.name(), warnings.get(0).getCode());
  }

  @Test
  void calculationAllocationMapper_checkResult2() {
    // SETUP
    final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
    final Holding h = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final Map.Entry<Holding, ClassificationAllocation> entry = mock(Map.Entry.class);

    when(entry.getKey()).thenReturn(h);

    final ClassificationAllocation classificationAllocation = mock(ClassificationAllocation.class);
    final Map<String, BigDecimal> emptyMap = Map.of();

    when(classificationAllocation.getSecurityClassificationValues()).thenReturn(emptyMap);
    when(entry.getValue()).thenReturn(classificationAllocation);
    when(classificationAllocation.getSecurityClassificationValues()).thenReturn(Map.of("TEST", BigDecimal.ONE));

    doCallRealMethod().when(m).getClassificationAllocationMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    final Map<ClassificationAllocationType, BigDecimal> actual = m.getClassificationAllocationMapper(entry, warnings);

    // VERIFY
    assertEquals(DEFAULT_MAP, actual);
    assertEquals(1, warnings.size());
    assertTrue(warnings.get(0).getMessage().contains("TEST"));
  }

  @Test
  void getClassificationAllocationMapper_checkResult3() {
    // SETUP
    final ClassificationAllocationCacheStorage m = mock(ClassificationAllocationCacheStorage.class);
    final Holding h = new Holding().setType(HoldingType.CASH).setValue(BigDecimal.ONE);
    final Map.Entry<Holding, ClassificationAllocation> entry = mock(Map.Entry.class);

    when(entry.getKey()).thenReturn(h);

    final ClassificationAllocation classificationAllocation = mock(ClassificationAllocation.class);
    final Map<String, BigDecimal> emptyMap = Map.of();

    when(classificationAllocation.getSecurityClassificationValues()).thenReturn(emptyMap);
    when(entry.getValue()).thenReturn(classificationAllocation);
    when(classificationAllocation.getSecurityClassificationValues()).thenReturn(Map.of(
        ClassificationAllocationType.ALTERNATIVE_INVESTMENTS__INTERNATIONAL.name(), BigDecimal.ONE));

    doCallRealMethod().when(m).getClassificationAllocationMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    final Map<ClassificationAllocationType, BigDecimal> actual = m.getClassificationAllocationMapper(entry, warnings);

    // VERIFY
    final HashMap<ClassificationAllocationType, BigDecimal> expected = new HashMap<>(DEFAULT_MAP);
    expected.put(ClassificationAllocationType.ALTERNATIVE_INVESTMENTS__INTERNATIONAL, BigDecimal.ONE);

    assertEquals(expected, actual);
    assertEquals(0, warnings.size());
  }

}