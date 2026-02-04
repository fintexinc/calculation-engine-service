package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.EquityMarketCapitalizationCacheStorage;
import com.fintex.ce.adapter.cache.repository.equitymarketcapitalization.EquityMarketCapitalizationRepository;
import com.fintex.ce.adapter.cache.repository.equitymarketcapitalization.EquityMarketCapitalizationStockRepository;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.MapUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.HoldingType.US_ETF;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.GIANT;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.LARGE;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.MEDIUM;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.MICRO;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.SMALL;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityMarketCapitalizationCacheStorageTest {

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var fdsRepo = mock(MultipleSMRepository.class);
      final var capitalizationRepository = mock(EquityMarketCapitalizationRepository.class);
      final var stockRepository = mock(EquityMarketCapitalizationStockRepository.class);
      final var cacheStatistic = mock(CacheStatisticService.class);

      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class, withSettings()
          .useConstructor(fdsRepo, null, capitalizationRepository, capitalizationRepository, capitalizationRepository,
              stockRepository, cacheStatistic));
      final List<Holding> holdings = List.of(mock(Holding.class));

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(mock(Warning.class));
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
      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<FundSeriesHolding> filtered = List.of(mock(FundSeriesHolding.class));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(mock(Warning.class));
      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadBenchOfFundCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(mock(Warning.class));
      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfEtfUs(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(mock(Warning.class));
      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfEtfCanada(filtered, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfBenchmarks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<BenchmarkIndexHolding> filtered = List.of(mock(BenchmarkIndexHolding.class));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(mock(Warning.class));
      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).loadForBenchOfBenchmarks(filtered, List.of());
    }
  }

  @Test
  void load_verifyMapForStocks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);
      final List<Holding> holdings = List.of(mock(Holding.class));

      doCallRealMethod().when(m).load(any(), any(), any(), any());
      // ACT
      final List<Warning> warnings = List.of(mock(Warning.class));
      m.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(m).mapForStocks(holdings, warnings);
    }
  }

  @Test
  void mapForStocks_verifyFilterHoldings() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var fdsRepo = mock(MultipleSMRepository.class);
      final var capitalizationRepository = mock(EquityMarketCapitalizationRepository.class);
      final var stockRepository = mock(EquityMarketCapitalizationStockRepository.class);
      final var cacheStatistic = mock(CacheStatisticService.class);

      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class, withSettings()
          .useConstructor(fdsRepo, null, capitalizationRepository, capitalizationRepository, capitalizationRepository,
              stockRepository, cacheStatistic));

      final List<Holding> holdings = List.of(mock(Holding.class));
      List<StockHolding> filtered = List.of(mock(StockHolding.class));
      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(m).mapForStocks(any(), any());
      // ACT
      final List<Warning> warnings = List.of(mock(Warning.class));
      m.mapForStocks(holdings, warnings);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
    }
  }

  @Test
  void mapForStocks_verifyConvertRatingsForStocks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var fdsRepo = mock(MultipleSMRepository.class);
      final var capitalizationRepository = mock(EquityMarketCapitalizationRepository.class);
      final var stockRepository = mock(EquityMarketCapitalizationStockRepository.class);
      final var cacheStatistic = mock(CacheStatisticService.class);

      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class, withSettings()
          .useConstructor(fdsRepo, null, capitalizationRepository, capitalizationRepository, capitalizationRepository,
              stockRepository, cacheStatistic));

      final List<Holding> holdings = List.of(mock(Holding.class));
      final Map<StockHolding, EquityMarketCapitalization> map = new HashMap<>();

      doCallRealMethod().when(m).mapForStocks(any(), any());
      // ACT
      final List<Warning> warnings = List.of(mock(Warning.class));
      m.mapForStocks(holdings, warnings);

      // VERIFY
      verify(m).convertRatingsForStocks(map, warnings);
    }
  }

  @Test
  void convertRatingsForStocks_verifyGetEquityMarketCapType() {
    // SETUP
    final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

    final StockHolding s = new StockHolding(ONE, US_ETF, "exCode", "testTicker");
    final EquityMarketCapitalization stock = new EquityMarketCapitalization(Map.of("GIANT", ONE));
    final Map<StockHolding, EquityMarketCapitalization> responseMap = Map.of(s, stock);

    doCallRealMethod().when(m).convertRatingsForStocks(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    m.convertRatingsForStocks(responseMap, warnings);

    // VERIFY
    verify(m).getEquityMarketCapType(s, "GIANT", warnings);
  }

  @Test
  void convertRatingsForStocks_verifyRatingForStocks() {
    // SETUP
    final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

    final StockHolding s = new StockHolding(ONE, US_ETF, "testExchangeCode", "testTicker");
    final EquityMarketCapitalization stock = new EquityMarketCapitalization(Map.of("GIANT", ONE));
    final Map<StockHolding, EquityMarketCapitalization> responseMap = Map.of(s, stock);

    doCallRealMethod().when(m).convertRatingsForStocks(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    m.convertRatingsForStocks(responseMap, warnings);

    // VERIFY
    verify(m).getEquityMarketCapType(s, "GIANT", warnings);
  }

  @Test
  void convertRatingsForStocks_verifyOverrideDefaultValues() {
    try (var mockedMapUtils = Mockito.mockStatic(MapUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);
      final EquityMarketCapitalization stock = mock(EquityMarketCapitalization.class);
      final Map<EquityMarketCapType, BigDecimal> DEFAULT_MAP = Map.of(GIANT, ZERO, LARGE, ZERO, MEDIUM, ZERO, SMALL,
          ZERO, MICRO, ZERO);
      final Map<StockHolding, EquityMarketCapitalization> responseMap = Map.of(mock(StockHolding.class), stock);

      when(stock.getRatings()).thenReturn(Map.of(GIANT.name(), ONE));
      when(m.getEquityMarketCapType(any(), any(), any())).thenReturn(GIANT);
      doCallRealMethod().when(m).convertRatingsForStocks(any(), any());
      // ACT
      final List<Warning> warnings = new ArrayList<>();
      m.convertRatingsForStocks(responseMap, warnings);

      // VERIFY
      mockedMapUtils.verify(() -> MapUtils.overrideDefaultValues(DEFAULT_MAP, Map.of(GIANT, ONE)));
    }
  }

  @Test
  void convertRatingsForStocks_checkResult() {
    try (var mockedMapUtils = Mockito.mockStatic(MapUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);
      final EquityMarketCapitalization stock = mock(EquityMarketCapitalization.class);
      final Map<StockHolding, EquityMarketCapitalization> responseMap = Map.of(mock(StockHolding.class), stock);

      when(stock.getRatings()).thenReturn(null);
      when(m.getEquityMarketCapType(any(), any(), any())).thenReturn(GIANT);
      doCallRealMethod().when(m).convertRatingsForStocks(any(), any());
      // ACT
      final List<Warning> warnings = new ArrayList<>();
      m.convertRatingsForStocks(responseMap, warnings);

      // VERIFY
      assertEquals(1, warnings.size());
      assertEquals("WRN_EMC_SBV_001", warnings.get(0).getCode());
      assertEquals("The holding is missing values for Style Box", warnings.get(0).getMessage());
    }
  }

  @Test
  void mapForNonStock_verifyConvertRatings() {
    // SETUP
    final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

    final Holding h = mock(Holding.class);
    final EquityMarketCapitalization r = mock(EquityMarketCapitalization.class);

    final Map<Holding, EquityMarketCapitalization> holdings = Map.of(h, r);
    final Map.Entry<Holding, EquityMarketCapitalization> entry = new AbstractMap.SimpleEntry<>(h, r);

    doCallRealMethod().when(m).mapForNonStock(any(), any());
    // ACT
    final List<Warning> warnings = List.of(mock(Warning.class));
    m.mapForNonStock(holdings, warnings);

    // VERIFY
    verify(m).convertRatings(entry, warnings);
  }

  @Test
  void convertRatings_checkResult() {
    // SETUP
    final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

    final StockHolding h = mock(StockHolding.class);
    final EquityMarketCapitalization r = mock(EquityMarketCapitalization.class);

    final Map.Entry<Holding, EquityMarketCapitalization> entry = new AbstractMap.SimpleEntry<>(h, r);

    doCallRealMethod().when(m).convertRatings(any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    m.convertRatings(entry, warnings);

    // VERIFY
    assertEquals(1, warnings.size());
  }

  @Test
  void convertRatings_verifyMapRatingsToRequiredFormat() {
    // SETUP
    final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

    final EquityMarketCapitalization r = new EquityMarketCapitalization(Map.of("test", ONE));
    final Map.Entry<Holding, EquityMarketCapitalization> entry = new AbstractMap.SimpleEntry<>(mock(StockHolding.class),
        r);
    final Map<String, BigDecimal> ratingsRaw = entry.getValue().getRatings();

    doCallRealMethod().when(m).convertRatings(any(), any());
    // ACT
    final List<Warning> warnings = List.of(mock(Warning.class));
    m.convertRatings(entry, warnings);

    // VERIFY
    verify(m).mapRatingsToRequiredFormat(entry.getKey(), ratingsRaw, warnings);
  }

  @Test
  void mapRatingsToRequiredFormat_verifyGetEquityMarketCapType() {
    // SETUP
    final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

    final Holding h = mock(Holding.class);
    final Map<String, BigDecimal> ratingsRaw = Map.of("GIANT", ONE);

    doCallRealMethod().when(m).mapRatingsToRequiredFormat(any(), any(), any());
    // ACT
    final List<Warning> warnings = List.of(mock(Warning.class));
    m.mapRatingsToRequiredFormat(h, ratingsRaw, warnings);

    // VERIFY
    verify(m).getEquityMarketCapType(h, "GIANT", warnings);
  }

  @Test
  void mapRatingsToRequiredFormat_verifyOverrideDefaultValues() {
    try (var mockedMapUtils = Mockito.mockStatic(MapUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

      final Map<EquityMarketCapType, BigDecimal> DEFAULT_MAP = Map.of(GIANT, ZERO, LARGE, ZERO, MEDIUM, ZERO, SMALL,
          ZERO, MICRO, ZERO);
      final Holding h = mock(Holding.class);
      final Map<String, BigDecimal> ratingsRaw = Map.of("GIANT", ONE);
      final Map<EquityMarketCapType, BigDecimal> ratings = Map.of(GIANT, ONE);

      when(m.getEquityMarketCapType(any(), any(), any())).thenReturn(GIANT);
      doCallRealMethod().when(m).mapRatingsToRequiredFormat(any(), any(), any());
      // ACT
      final List<Warning> warnings = new ArrayList<>();
      m.mapRatingsToRequiredFormat(h, ratingsRaw, warnings);

      // VERIFY
      mockedMapUtils.verify(() -> MapUtils.overrideDefaultValues(DEFAULT_MAP, ratings));
    }
  }

  @Test
  void getEquityMarketCapType_checkResult() {
    // SETUP
    final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

    final Holding h = mock(Holding.class);
    final String s = "Large";

    doCallRealMethod().when(m).getEquityMarketCapType(any(), any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    EquityMarketCapType e = m.getEquityMarketCapType(h, s, warnings);

    // VERIFY
    assertEquals(LARGE, e);
  }

  @Test
  void getEquityMarketCapType_checkWarningSize() {
    // SETUP
    final EquityMarketCapitalizationCacheStorage m = mock(EquityMarketCapitalizationCacheStorage.class);

    final Holding h = mock(Holding.class);
    final String s = "test";

    doCallRealMethod().when(m).getEquityMarketCapType(any(), any(), any());
    // ACT
    final List<Warning> warnings = new ArrayList<>();
    m.getEquityMarketCapType(h, s, warnings);

    // VERIFY
    assertEquals(1, warnings.size());
  }
}