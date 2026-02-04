package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.CommonHoldingsCacheStorage;
import com.fintex.ce.adapter.cache.repository.commonholdings.CommonHoldingsRepository;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.CommonHoldingsDTO;
import com.fintex.ce.domain.model.CommonHoldingsStock;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_TCH_MUH_002;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_TCH_MUH_001;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CommonHoldingsCacheStorageTest {

  private static final String EQUITY_TYPE = "E";

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      var set = mock(Set.class);
      var fdsRepo = mock(MultipleSMRepository.class);
      var holdingRepository = mock(CommonHoldingsRepository.class);
      var cacheStatisticService = mock(CacheStatisticService.class);

      var cacheStorage = mock(CommonHoldingsCacheStorage.class, withSettings()
          .useConstructor(set, fdsRepo, null, holdingRepository, holdingRepository, holdingRepository,
              holdingRepository, cacheStatisticService));

      var holdings = List.of(mock(Holding.class));
      var warnings = List.of(mock(Warning.class));

      doCallRealMethod().when(cacheStorage).load(any(), any(), any(), any());
      // ACT
      cacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
    }
  }

  @Test
  void load_verifyLoadBenchOfFundCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      var cacheStorage = mock(CommonHoldingsCacheStorage.class);
      var holdings = List.of(mock(Holding.class));
      var filteredHoldings = List.of(mock(FundSeriesHolding.class));
      var warnings = List.of(mock(Warning.class));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filteredHoldings);

      doCallRealMethod().when(cacheStorage).load(any(), any(), any(), any());
      // ACT
      cacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(cacheStorage).loadBenchOfFundCanada(filteredHoldings, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfBenchmarks() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      var cacheStorage = mock(CommonHoldingsCacheStorage.class);
      var holdings = List.of(mock(Holding.class));
      var filteredHoldings = List.of(mock(BenchmarkIndexHolding.class));
      var warnings = List.of(mock(Warning.class));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(
          filteredHoldings);

      doCallRealMethod().when(cacheStorage).load(any(), any(), any(), any());
      // ACT
      cacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(cacheStorage).loadForBenchOfBenchmarks(filteredHoldings, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfUs() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      var cacheStorage = mock(CommonHoldingsCacheStorage.class);
      var holdings = List.of(mock(Holding.class));
      var filteredHoldings = List.of(mock(EtfHolding.class));
      var warnings = List.of(mock(Warning.class));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(
          filteredHoldings);

      doCallRealMethod().when(cacheStorage).load(any(), any(), any(), any());
      // ACT
      cacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(cacheStorage).loadForBenchOfEtfUs(filteredHoldings, List.of());
    }
  }

  @Test
  void load_verifyLoadForBenchOfEtfCanada() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      var cacheStorage = mock(CommonHoldingsCacheStorage.class);
      var holdings = List.of(mock(Holding.class));
      var filteredHoldings = List.of(mock(EtfHolding.class));
      var warnings = List.of(mock(Warning.class));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filteredHoldings);

      doCallRealMethod().when(cacheStorage).load(any(), any(), any(), any());
      // ACT
      cacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

      // VERIFY
      verify(cacheStorage).loadForBenchOfEtfCanada(filteredHoldings, List.of());
    }
  }

  @Test
  void mapForStock_checkResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
    var holding = mock(Holding.class);
    var stockHoldings = Map.of(holding, mock(CommonHoldingsStock.class));
    var paramHolderDTO = mock(ParamHolderDTO.class);

    when(cacheStorage.initializeStockCommonHoldingsDTO(any(), any())).thenReturn(commonHoldingsDTO);

    doCallRealMethod().when(cacheStorage).mapForStock(anyMap(), any());
    // ACT
    var expected = cacheStorage.mapForStock(stockHoldings, paramHolderDTO);

    // VERIFY
    assertEquals(Map.of(holding, List.of(commonHoldingsDTO)), expected);
  }

  @Test
  void mapForStock_verifyInitializeStockCommonHoldingsDTO() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
    var holding = mock(Holding.class);
    var commonHoldingsStock = mock(CommonHoldingsStock.class);
    var stockHoldings = Map.of(holding, commonHoldingsStock);
    var stockHolding = new AbstractMap.SimpleEntry<>(holding, commonHoldingsStock);
    var paramHolderDTO = mock(ParamHolderDTO.class);

    when(cacheStorage.initializeStockCommonHoldingsDTO(any(), any())).thenReturn(commonHoldingsDTO);

    doCallRealMethod().when(cacheStorage).mapForStock(anyMap(), any());
    // ACT
    cacheStorage.mapForStock(stockHoldings, paramHolderDTO);

    // VERIFY
    verify(cacheStorage).initializeStockCommonHoldingsDTO(stockHolding, paramHolderDTO);
  }

  @Test
  void initializeStockCommonHoldingsDTO_verifyCalculateStockHoldingValue() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldingsStock = mock(CommonHoldingsStock.class);
    var stockHolding = new AbstractMap.SimpleEntry<>(holding, commonHoldingsStock);
    var paramHolderDTO = mock(ParamHolderDTO.class);

    when(commonHoldingsStock.getCompanyName()).thenReturn("Tesla");
    when(commonHoldingsStock.getTicker()).thenReturn("TICKER");
    when(commonHoldingsStock.getExchangeCode()).thenReturn("EXCHANGE_CODE");
    when(holding.getValue()).thenReturn(TEN);

    var expected = new CommonHoldingsDTO("Tesla", EQUITY_TYPE, TEN, "TICKER", "EXCHANGE_CODE");

    doCallRealMethod().when(cacheStorage).initializeStockCommonHoldingsDTO(any(), any());
    // ACT
    var actual = cacheStorage.initializeStockCommonHoldingsDTO(stockHolding, paramHolderDTO);

    // VERIFY
    verify(cacheStorage).calculateStockHoldingValue(stockHolding, paramHolderDTO);
  }

  @Test
  void initializeStockCommonHoldingsDTO_checkResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldingsStock = mock(CommonHoldingsStock.class);
    var stockHolding = new AbstractMap.SimpleEntry<>(holding, commonHoldingsStock);
    var paramHolderDTO = mock(ParamHolderDTO.class);

    when(commonHoldingsStock.getCompanyName()).thenReturn("Tesla");
    when(commonHoldingsStock.getTicker()).thenReturn("TICKER");
    when(commonHoldingsStock.getExchangeCode()).thenReturn("EXCHANGE_CODE");
    when(holding.getValue()).thenReturn(TEN);
    when(cacheStorage.calculateStockHoldingValue(any(), any())).thenReturn(ONE);

    var expected = new CommonHoldingsDTO("Tesla", EQUITY_TYPE, ONE, "TICKER", "EXCHANGE_CODE");

    doCallRealMethod().when(cacheStorage).initializeStockCommonHoldingsDTO(any(), any());
    // ACT
    var actual = cacheStorage.initializeStockCommonHoldingsDTO(stockHolding, paramHolderDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculateStockHoldingValue_checkResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldingsStock = mock(CommonHoldingsStock.class);
    var stockHolding = new AbstractMap.SimpleEntry<>(holding, commonHoldingsStock);
    var paramHolderDTO = mock(ParamHolderDTO.class);
    var allocations = Map.of(holding, TEN);
    var expected = TEN;

    when(holding.generateUserIdentifier()).thenReturn("test");
    when(paramHolderDTO.getAllocations()).thenReturn(allocations);
    when(commonHoldingsStock.getCompanyName()).thenReturn("Tesla");
    when(commonHoldingsStock.getTicker()).thenReturn("TICKER");
    when(commonHoldingsStock.getExchangeCode()).thenReturn("EXCHANGE_CODE");
    when(holding.getValue()).thenReturn(TEN);

    doCallRealMethod().when(cacheStorage).calculateStockHoldingValue(any(), any());
    // ACT
    var actual = cacheStorage.calculateStockHoldingValue(stockHolding, paramHolderDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void mapForNoneStock_verifyMapNoneStock() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holdings = new HashMap<>(Map.of(mock(Holding.class), mock(CommonHoldings.class)));
    var warnings = List.of(mock(Warning.class));

    var notification = new Notification();
    doNothing().when(cacheStorage).validate(anyMap(), anyList(), any());

    doCallRealMethod().when(cacheStorage).mapForNoneStock(anyMap());
    // ACT
    cacheStorage.mapForNoneStock(holdings);

    // VERIFY
    verify(cacheStorage).mapNoneStock(holdings);
  }

  @Test
  void mapForNoneStock_checkResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holdings = new HashMap<Holding, CommonHoldings>();
    var warnings = List.of(mock(Warning.class));
    var expected = new HashMap<Holding, List<CommonHoldingsDTO>>();
    var notification = new Notification();

    doCallRealMethod().when(cacheStorage).mapForNoneStock(anyMap());
    // ACT
    var actual = cacheStorage.mapForNoneStock(holdings);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void mapForNoneStock_checkResult2() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var holdings = new HashMap<>(Map.of(holding, mock(CommonHoldings.class)));
    var warnings = List.of(mock(Warning.class));
    var expected = new HashMap<>(Map.of(holding, List.of(mock(CommonHoldingsDTO.class))));
    var notification = new Notification();

    doNothing().when(cacheStorage).validate(anyMap(), anyList(), any());
    when(cacheStorage.mapNoneStock(anyMap())).thenReturn(expected);

    doCallRealMethod().when(cacheStorage).mapForNoneStock(anyMap());
    // ACT
    var actual = cacheStorage.mapForNoneStock(holdings);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void mapNoneStock_checkResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldings = mock(CommonHoldings.class);
    var holdings = new HashMap<>(Map.of(holding, commonHoldings));
    var commonHolding = mock(CommonHoldings.CommonHolding.class);
    var expected = new HashMap<>(Map.of(holding, List.of(new CommonHoldingsDTO())));

    when(commonHoldings.getHoldings()).thenReturn(List.of(commonHolding));

    doCallRealMethod().when(cacheStorage).mapNoneStock(anyMap());
    // ACT
    var actual = cacheStorage.mapNoneStock(holdings);

    // VERIFY
    assertEquals(1, actual.size());
    assertTrue(actual.containsKey(holding));
  }

  @Test
  void validate_checkResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldings = mock(CommonHoldings.class);
    var holdings = new HashMap<>(Map.of(holding, commonHoldings));
    var warnings = List.of(mock(Warning.class));
    var notification = new Notification();

    when(commonHoldings.getHoldings()).thenReturn(null);

    doCallRealMethod().when(cacheStorage).validate(anyMap(), anyList(), any());
    // ACT
    cacheStorage.validate(holdings, warnings, notification);

    // VERIFY
    assertTrue(notification.getErrors().stream().anyMatch(e -> e.getCode().equals(ERR_TCH_MUH_002)));
  }

  @Test
  void validate_verifyCheckWarnings() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldings = mock(CommonHoldings.class);
    var holdings = new HashMap<>(Map.of(holding, commonHoldings));
    var warnings = List.of(mock(Warning.class));
    var notification = new Notification();
    var commonHolding = mock(CommonHoldings.CommonHolding.class);

    when(commonHoldings.getHoldings()).thenReturn(List.of(commonHolding));
    doNothing().when(cacheStorage).checkWarnings(anyMap(), anyList());

    doCallRealMethod().when(cacheStorage).validate(anyMap(), anyList(), any());
    // ACT
    cacheStorage.validate(holdings, warnings, notification);

    // VERIFY
    verify(cacheStorage).checkWarnings(holdings, warnings);
  }

  @Test
  void checkWarnings_verifyIsWarningsPresent() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holdings = new HashMap<>(Map.of(mock(Holding.class), mock(CommonHoldings.class)));
    var warnings = List.of(mock(Warning.class));

    when(cacheStorage.isWarningPresent(anyMap())).thenReturn(false);

    doCallRealMethod().when(cacheStorage).checkWarnings(anyMap(), anyList());
    // ACT
    cacheStorage.checkWarnings(holdings, warnings);

    // VERIFY
    verify(cacheStorage).isWarningPresent(holdings);
  }

  @Test
  void checkWarnings_verifyCheckResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldings = mock(CommonHoldings.class);
    var holdings = new HashMap<>(Map.of(holding, commonHoldings));
    var warnings = new ArrayList<Warning>();
    var expected = List.of(new Warning("test_ID", WRN_TCH_MUH_001.getMessage(), WRN_TCH_MUH_001.name()));

    when(holding.generateUserIdentifier()).thenReturn("test_ID");
    when(cacheStorage.isWarningPresent(anyMap())).thenReturn(true);

    doCallRealMethod().when(cacheStorage).checkWarnings(anyMap(), anyList());
    // ACT
    cacheStorage.checkWarnings(holdings, warnings);

    // VERIFY
    assertEquals(expected.get(0).getMessage(), warnings.get(0).getMessage());
  }

  @Test
  void isWarningPresent_checkPositiveResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldings = mock(CommonHoldings.class);
    var holdings = new HashMap<>(Map.of(holding, commonHoldings));
    var commonHolding = mock(CommonHoldings.CommonHolding.class);
    cacheStorage.firstLvlRecursionTypes = Set.of("FE");

    when(commonHoldings.getHoldings()).thenReturn(List.of(commonHolding));
    when(commonHolding.getType()).thenReturn("FE");
    when(commonHolding.getUnderlyingHoldings()).thenReturn(null);

    doCallRealMethod().when(cacheStorage).isWarningPresent(anyMap());
    // ACT
    var actual = cacheStorage.isWarningPresent(holdings);

    // VERIFY
    Assertions.assertTrue(actual);
  }

  @Test
  void isWarningPresent_checkNegativeResult() {
    // SETUP
    var cacheStorage = mock(CommonHoldingsCacheStorage.class);
    var holding = mock(Holding.class);
    var commonHoldings = mock(CommonHoldings.class);
    var holdings = new HashMap<>(Map.of(holding, commonHoldings));
    var commonHolding = mock(CommonHoldings.CommonHolding.class);
    cacheStorage.firstLvlRecursionTypes = Set.of("FE");

    when(commonHoldings.getHoldings()).thenReturn(List.of(commonHolding));
    when(commonHolding.getType()).thenReturn("E");
    when(commonHolding.getUnderlyingHoldings()).thenReturn(List.of(commonHolding));

    doCallRealMethod().when(cacheStorage).isWarningPresent(anyMap());
    // ACT
    var actual = cacheStorage.isWarningPresent(holdings);

    // VERIFY
    Assertions.assertFalse(actual);
  }
}