package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.EquitySectorCacheStorage;
import com.fintex.ce.adapter.cache.entity.equitysector.REquitySector;
import com.fintex.ce.adapter.cache.entity.equitysector.REquitySectorStock;
import com.fintex.ce.adapter.cache.repository.equitysector.EquitySectorRepository;
import com.fintex.ce.adapter.cache.repository.equitysector.EquitySectorStockRepository;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.EquitySectorStock;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.adapter.cache.EquitySectorCacheStorage.DEFAULT_MAP;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_ES_ESA_001;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquitySectorCacheStorageTest {

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var fdsRepo = mock(SecurityDataPort.class);
      final var mapper = mock(CacheEntityMapper.class);
      final var stockMapper = mock(CacheEntityMapper.class);
      final var sectorRepo = mock(EquitySectorRepository.class);
      final var stockRepository = mock(EquitySectorStockRepository.class);

      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class, withSettings()
          .useConstructor(fdsRepo, mapper, stockMapper, sectorRepo, stockRepository));

      final List<Holding> holdings = List.of(new Holding());

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
      verify(m).mapForStocks(holdings, warnings);
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());

      final List<FundSeriesHolding> filtered = List.of(new FundSeriesHolding().setFundServCode("TEST"));
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
  void load_verifyLoadForBenchOfBenchmarks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());

      final List<BenchmarkIndexHolding> filtered = List.of(new BenchmarkIndexHolding().setMrStarId("TEST"));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfBenchmarks(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());

      final List<EtfHolding> filtered = List.of(new EtfHolding().setTicker("TEST").setExchangeCode("TST"));
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
      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());

      final List<EtfHolding> filtered = List.of(new EtfHolding().setTicker("TEST").setExchangeCode("TST"));
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
  void mapForStocks_verifyFilterHoldingsForStocks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var fdsRepo = mock(SecurityDataPort.class);
      final var mapper = mock(CacheEntityMapper.class);
      final var stockMapper = mock(CacheEntityMapper.class);
      final var sectorRepo = mock(EquitySectorRepository.class);
      final var stockRepository = mock(EquitySectorStockRepository.class);

      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class, withSettings()
          .useConstructor(fdsRepo, mapper, stockMapper, sectorRepo, stockRepository));

      final List<Holding> holdings = List.of(new Holding());
      final List<Warning> warnings = new ArrayList<>();

      final List<StockHolding> filtered = List.of();
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(m).mapForStocks(any(), anyList());
      // ACT
      m.mapForStocks(holdings, warnings);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
    }
  }

  @Test
  void load_verifyMapForStocks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).mapForStocks(holdings, warnings);
    }
  }

  @Test
  void mapForStocks_checkResultWithInvalidSectorThrowsException() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var fdsRepo = mock(SecurityDataPort.class);
      final CacheEntityMapper<EquitySector, REquitySector> mapper = mock(CacheEntityMapper.class);
      final CacheEntityMapper<EquitySectorStock, REquitySectorStock> stockMapper = mock(CacheEntityMapper.class);
      final var sectorRepo = mock(EquitySectorRepository.class);
      final var stockRepository = mock(EquitySectorStockRepository.class);

      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class, withSettings()
          .useConstructor(fdsRepo, mapper, stockMapper, sectorRepo, stockRepository));

      final List<Warning> warnings = new ArrayList<>();
      final var h = new StockHolding().setTicker("TEST").setExchangeCode("TST");
      h.setType(HoldingType.US_STOCKS);

      final List<Holding> holdings = List.of(h);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(List.of(
          h));

      final REquitySectorStock rEquitySectorStock = mock(REquitySectorStock.class);
      final EquitySectorStock sectorStock = new EquitySectorStock();
      sectorStock.setSectorName("INVALID_SECTOR");

      when(stockRepository.findAllByHoldingId(any())).thenReturn(List.of(rEquitySectorStock));
      when(stockMapper.toDomain(rEquitySectorStock)).thenReturn(java.util.Optional.of(sectorStock));

      doCallRealMethod().when(m).mapForStocks(any(), any());
      // ACT
      assertThrows(DataErrorException.class, () -> m.mapForStocks(holdings, warnings));
    }
  }

  @Test
  void mapForStocks_checkResultWithValidSector() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var fdsRepo = mock(SecurityDataPort.class);
      final CacheEntityMapper<EquitySector, REquitySector> mapper = mock(CacheEntityMapper.class);
      final CacheEntityMapper<EquitySectorStock, REquitySectorStock> stockMapper = mock(CacheEntityMapper.class);
      final var sectorRepo = mock(EquitySectorRepository.class);
      final var stockRepository = mock(EquitySectorStockRepository.class);

      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class, withSettings()
          .useConstructor(fdsRepo, mapper, stockMapper, sectorRepo, stockRepository));

      final var h = new StockHolding().setTicker("TEST").setExchangeCode("TST");
      h.setType(HoldingType.US_STOCKS);
      final List<Warning> warnings = new ArrayList<>();

      final List<Holding> holdings = List.of(h);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(List.of(
          h));

      final REquitySectorStock rEquitySectorStock = mock(REquitySectorStock.class);
      final EquitySectorStock sectorStock = new EquitySectorStock();
      sectorStock.setSectorName(EquitySectorAllocationType.BASIC_MATERIALS.name());

      when(stockRepository.findAllByHoldingId(any())).thenReturn(List.of(rEquitySectorStock));
      when(stockMapper.toDomain(rEquitySectorStock)).thenReturn(java.util.Optional.of(sectorStock));

      doCallRealMethod().when(m).mapForStocks(any(), any());
      // ACT
      final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> actual = m.mapForStocks(holdings, warnings);

      // VERIFY
      assertEquals(1, actual.size());
      assertEquals(BigDecimal.ONE, actual.get(h).get(EquitySectorAllocationType.BASIC_MATERIALS));
    }
  }

  @Test
  void mapForStocks_checkResultWithNullSectorNameAddsWarning() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var fdsRepo = mock(SecurityDataPort.class);
      final CacheEntityMapper<EquitySector, REquitySector> mapper = mock(CacheEntityMapper.class);
      final CacheEntityMapper<EquitySectorStock, REquitySectorStock> stockMapper = mock(CacheEntityMapper.class);
      final var sectorRepo = mock(EquitySectorRepository.class);
      final var stockRepository = mock(EquitySectorStockRepository.class);

      final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class, withSettings()
          .useConstructor(fdsRepo, mapper, stockMapper, sectorRepo, stockRepository));

      final var h = new StockHolding().setTicker("TEST").setExchangeCode("TST");
      h.setType(HoldingType.US_STOCKS);
      final List<Warning> warnings = new ArrayList<>();

      final List<Holding> holdings = List.of(h);
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(List.of(
          h));

      final REquitySectorStock rEquitySectorStock = mock(REquitySectorStock.class);
      final EquitySectorStock sectorStock = new EquitySectorStock();
      sectorStock.setSectorName(null);

      when(stockRepository.findAllByHoldingId(any())).thenReturn(List.of(rEquitySectorStock));
      when(stockMapper.toDomain(rEquitySectorStock)).thenReturn(java.util.Optional.of(sectorStock));

      doCallRealMethod().when(m).mapForStocks(any(), any());
      // ACT
      final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> actual = m.mapForStocks(holdings, warnings);

      // VERIFY
      assertEquals(0, actual.size());
      assertEquals(1, warnings.size());
      assertEquals("The holding is missing values for Sector Name", warnings.get(0).getMessage());
      assertEquals("WRN_ES_SN_001", warnings.get(0).getCode());
    }
  }

  @Test
  void DEFAULT_MAP_checkResult() {
    // SETUP

    // ACT

    // VERIFY
    assertEquals(EquitySectorAllocationType.values().length, DEFAULT_MAP.size());
    assertEquals(BigDecimal.ZERO, DEFAULT_MAP.get(EquitySectorAllocationType.BASIC_MATERIALS));
  }

  @Test
  void mapForNoneStock_verifyEquitySectorAllocationMapper() {
    // SETUP
    final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

    final Holding h = new Holding();
    final EquitySector rEquitySector = mock(EquitySector.class);
    final Map<Holding, EquitySector> sectorMap = Map.of(h, rEquitySector);

    doCallRealMethod().when(m).mapForNoneStock(any(), any());
    // ACT
    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    m.mapForNoneStock(sectorMap, warnings);

    // VERIFY
    verify(m).equitySectorAllocationMapper(
        argThat(arg -> arg.getKey() == h && arg.getValue() == rEquitySector),
        eq(warnings));
  }

  @Test
  void mapForNoneStock_checkResult() {
    // SETUP
    final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

    final Holding h = new Holding();
    final EquitySector rEquitySector = mock(EquitySector.class);
    final Map<Holding, EquitySector> sectorMap = Map.of(h, rEquitySector);

    final Map<EquitySectorAllocationType, BigDecimal> actualValue = Map.of(EquitySectorAllocationType.BASIC_MATERIALS,
        BigDecimal.TEN);
    when(m.equitySectorAllocationMapper(any(), any())).thenReturn(actualValue);

    doCallRealMethod().when(m).mapForNoneStock(any(), any());
    // ACT
    final List<Warning> warnings = List.of(new Warning("id", "msg", "code"));
    final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> actual = m.mapForNoneStock(sectorMap, warnings);

    // VERIFY
    assertEquals(Map.of(h, actualValue), actual);
  }

  @Test
  void equitySectorAllocationMapper_checkResult() {
    // SETUP
    final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

    final Holding h = new Holding();

    final Map.Entry entry = mock(Map.Entry.class);
    when(entry.getKey()).thenReturn(h);

    final EquitySector sector = mock(EquitySector.class);
    when(entry.getValue()).thenReturn(sector);

    when(sector.getAllocations()).thenReturn(Map.of());

    doCallRealMethod().when(m).equitySectorAllocationMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    final Map actual = m.equitySectorAllocationMapper(entry, warnings);

    // VERIFY
    assertEquals(DEFAULT_MAP, actual);
    assertEquals(1, warnings.size());
    assertEquals(WRN_ES_ESA_001.getMessage(), warnings.get(0).getMessage());
    assertEquals(WRN_ES_ESA_001.name(), warnings.get(0).getCode());
  }

  @Test
  void equitySectorAllocationMapper_checkResult2() {
    // SETUP
    final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

    final Holding h = new Holding();

    final Map.Entry entry = mock(Map.Entry.class);
    when(entry.getKey()).thenReturn(h);

    final EquitySector sector = mock(EquitySector.class);
    when(entry.getValue()).thenReturn(sector);

    when(sector.getAllocations()).thenReturn(Map.of(EquitySectorAllocationType.BASIC_MATERIALS, BigDecimal.ONE));

    doCallRealMethod().when(m).equitySectorAllocationMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    final Map actual = m.equitySectorAllocationMapper(entry, warnings);

    // VERIFY
    final HashMap<EquitySectorAllocationType, BigDecimal> expected = new HashMap<>(DEFAULT_MAP);
    expected.put(EquitySectorAllocationType.BASIC_MATERIALS, BigDecimal.ONE);
    assertEquals(expected, actual);
    assertEquals(0, warnings.size());
  }

  @Test
  void equitySectorAllocationMapper_checkResult3() {
    // SETUP
    final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

    final Holding h = new Holding();

    final Map.Entry entry = mock(Map.Entry.class);
    when(entry.getKey()).thenReturn(h);

    final EquitySector sector = mock(EquitySector.class);
    when(entry.getValue()).thenReturn(sector);

    when(sector.getAllocations()).thenReturn(Map.of(EquitySectorAllocationType.TECHNOLOGY, BigDecimal.TEN));

    doCallRealMethod().when(m).equitySectorAllocationMapper(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    final Map actual = m.equitySectorAllocationMapper(entry, warnings);

    // VERIFY
    final HashMap<EquitySectorAllocationType, BigDecimal> expected = new HashMap<>(DEFAULT_MAP);
    expected.put(EquitySectorAllocationType.TECHNOLOGY, BigDecimal.TEN);

    assertEquals(expected, actual);
    assertEquals(0, warnings.size());
  }

}
