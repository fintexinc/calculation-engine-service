package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.AverageMERCacheStorage;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.fintex.ce.domain.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.util.FilterUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ManagementExpenseAbstractCacheStorageTest {

  @Test
  void load_verifyCanadaMutualFund() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<Holding> filtered = List.of(mock(Holding.class));

      final Map benchFunds = new HashMap<>();
      when(sut.loadBenchOfFundCanada(anyList(), any())).thenReturn(benchFunds);
      final AverageManagementExpenseCalculationDTO merDTO = mock(AverageManagementExpenseCalculationDTO.class);
      when(sut.mapperForCanadaMutualFund(any(), any())).thenReturn(merDTO);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any());
      // ACT
      final List<DataProvider> eagle = List.of(EAGLE);
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load = sut.load(holdings, eagle, List
          .of(), new ParamHolderDTO());

      // VERIFY
      verify(sut).addHoldingsToResult(
          eq(filtered),
          eq(eagle),
          eq(load),
          argThat(arg -> arg.apply(List.of(), List.of()) == benchFunds),
          isA(BiConsumer.class),
          argThat(arg -> arg.apply(null, null) == merDTO));
    }
  }

  @Test
  void load_verifyCanadaEtfFund() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<Holding> filtered = List.of(mock(Holding.class));

      final Map benchFunds = new HashMap<>();
      when(sut.loadForBenchOfEtfCanada(anyList(), any())).thenReturn(benchFunds);
      final AverageManagementExpenseCalculationDTO merDTO = mock(AverageManagementExpenseCalculationDTO.class);
      when(sut.mapperForEtfCanada(any(), any())).thenReturn(merDTO);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any());
      // ACT
      final List<DataProvider> eagle = List.of(EAGLE);
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load = sut.load(holdings, eagle, List
          .of(), new ParamHolderDTO());

      // VERIFY
      verify(sut).addHoldingsToResult(
          eq(filtered),
          eq(eagle),
          eq(load),
          argThat(arg -> arg.apply(List.of(), List.of()) == benchFunds),
          isA(BiConsumer.class),
          argThat(arg -> arg.apply(null, null) == merDTO));
    }
  }

  @Test
  void load_verifyUsEtfFund() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<Holding> filtered = List.of(mock(Holding.class));

      final Map benchFunds = new HashMap<>();
      when(sut.loadForBenchOfEtfUs(anyList(), any())).thenReturn(benchFunds);
      final AverageManagementExpenseCalculationDTO merDTO = mock(AverageManagementExpenseCalculationDTO.class);
      when(sut.mapperForEtfUs(any(), any())).thenReturn(merDTO);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any());
      // ACT
      final List<DataProvider> eagle = List.of(EAGLE);
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load = sut.load(holdings, eagle, List
          .of(), new ParamHolderDTO());

      // VERIFY
      verify(sut).addHoldingsToResult(
          eq(filtered),
          eq(eagle),
          eq(load),
          argThat(arg -> arg.apply(List.of(), List.of()) == benchFunds),
          isA(BiConsumer.class),
          argThat(arg -> arg.apply(null, null) == merDTO));
    }
  }

  @Test
  void load_verifyCashFund() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<Holding> filtered = List.of(mock(Holding.class));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CASH_PREDICATE))).thenReturn(filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any());
      // ACT
      final List<DataProvider> eagle = List.of(EAGLE);
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load = sut.load(holdings, eagle, List
          .of(), new ParamHolderDTO());

      // VERIFY
      verify(sut).addHoldingsToResult(filtered, load);
    }
  }

  @Test
  void load_verifyUsStockFund() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<Holding> filtered = List.of(mock(Holding.class));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_STOCKS_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any());
      // ACT
      final List<DataProvider> eagle = List.of(EAGLE);
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load = sut.load(holdings, eagle, List
          .of(), new ParamHolderDTO());

      // VERIFY
      verify(sut).addHoldingsToResult(filtered, load);
    }
  }

  @Test
  void load_verifyCanadaStockFund() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final List<Holding> holdings = List.of(mock(Holding.class));
      final List<Holding> filtered = List.of(mock(Holding.class));

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_STOCKS_PREDICATE))).thenReturn(
          filtered);

      doCallRealMethod().when(sut).load(any(), any(), any(), any());
      // ACT
      final List<DataProvider> eagle = List.of(EAGLE);
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> load = sut.load(holdings, eagle, List
          .of(), new ParamHolderDTO());

      // VERIFY
      verify(sut).addHoldingsToResult(filtered, load);
    }
  }

  @Test
  void addHoldingsToResult_checkResult() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final AverageManagementExpenseCalculationDTO merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    when(sut.mapperForHolding(any())).thenReturn(merDTO);

    final Holding h = mock(Holding.class);
    when(h.getType()).thenReturn(HoldingType.CASH);

    doCallRealMethod().when(sut).addHoldingsToResult(any(), any());
    // ACT
    final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> resultMap = new HashMap<>();
    sut.addHoldingsToResult(List.of(h), resultMap);

    // VERIFY
    assertTrue(resultMap.containsKey(HoldingType.CASH));
    assertEquals(Map.of(h, merDTO), resultMap.get(HoldingType.CASH));
  }

  @Test
  void addHoldingsToResult_checkResult2() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final AverageManagementExpenseCalculationDTO merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    when(sut.mapperForHolding(any())).thenReturn(merDTO);

    final Holding h = mock(Holding.class);
    when(h.getType()).thenReturn(HoldingType.CASH);

    final BiFunction fdsCall = mock(BiFunction.class);
    final RedisId redisId = mock(RedisId.class);
    when(fdsCall.apply(any(), any())).thenReturn(Map.of(h, redisId));

    final BiConsumer dataProviderChecker = mock(BiConsumer.class);

    final BiFunction mapper = mock(BiFunction.class);
    when(mapper.apply(any(), any())).thenReturn(merDTO);

    final List<Holding> holdings = List.of(h);

    doCallRealMethod().when(sut).addHoldingsToResult(any(), any(), any(), any(), any(), any());
    // ACT
    final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> resultMap = new HashMap<>();
    final List<DataProvider> eagle = List.of(EAGLE);
    sut.addHoldingsToResult(holdings, eagle, resultMap, fdsCall, dataProviderChecker, mapper);

    // VERIFY
    assertTrue(resultMap.containsKey(HoldingType.CASH));
    assertEquals(Map.of(h, merDTO), resultMap.get(HoldingType.CASH));

    verify(fdsCall).apply(holdings, eagle);
    verify(mapper).apply(h, redisId);
  }

  @Test
  void addHoldingsToResult_checkResult3() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    when(sut.mapperForHolding(any())).thenReturn(merDTO);

    final var holding = mock(Holding.class);
    when(holding.getType()).thenReturn(HoldingType.CASH);

    final var fdsCall = mock(BiFunction.class);
    final var redisId = mock(RedisId.class);
    when(fdsCall.apply(any(), any())).thenReturn(Map.of(holding, redisId));

    final var dataProviderChecker = mock(BiConsumer.class);

    final var mapper = mock(BiFunction.class);
    when(mapper.apply(any(), any())).thenReturn(merDTO);

    final List<Holding> holdings = List.of();

    doCallRealMethod().when(sut).addHoldingsToResult(any(), any(), any(), any(), any(), any());
    // ACT
    final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> resultMap = new HashMap<>();
    final var dataProviders = List.of(EAGLE);
    sut.addHoldingsToResult(holdings, dataProviders, resultMap, fdsCall, dataProviderChecker, mapper);

    // VERIFY
    assertTrue(resultMap.isEmpty());
  }

  @Test
  void addHoldingsToResult_checkResult4() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    when(sut.mapperForHolding(any())).thenReturn(merDTO);

    final var holding = mock(Holding.class);
    when(holding.getType()).thenReturn(HoldingType.CASH);

    final var fdsCall = mock(BiFunction.class);
    final var redisId = mock(RedisId.class);
    when(fdsCall.apply(any(), any())).thenReturn(Map.of(holding, redisId));

    final var mapper = mock(BiFunction.class);
    when(mapper.apply(any(), any())).thenReturn(merDTO);

    final List<Holding> holdings = List.of();

    doCallRealMethod().when(sut).addHoldingsToResult(any(), any());
    // ACT
    final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> resultMap = new HashMap<>();
    sut.addHoldingsToResult(holdings, resultMap);

    // VERIFY
    assertTrue(resultMap.isEmpty());
  }

  @Test
  void preBuildAverageMerDto_checkResult() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final Holding h = mock(Holding.class);
    when(h.getType()).thenReturn(HoldingType.CASH);
    when(h.getValue()).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(sut).preBuildAverageMerDto(any());
    // ACT
    final AverageManagementExpenseCalculationDTO actual = sut.preBuildAverageMerDto(h);

    // VERIFY
    assertEquals(h.getType(), actual.getHoldingType());
    assertEquals(h.getValue(), actual.getMarketValue());
  }

  @Test
  void mapperForHolding_checkResult() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    final Holding h = mock(Holding.class);

    doCallRealMethod().when(sut).mapperForHolding(any());
    // ACT
    sut.mapperForHolding(h);

    // VERIFY
    verify(merDTO).setInitialFee(BigDecimal.ZERO);
    verify(merDTO).setModifiedFee(BigDecimal.ZERO);
  }

  @Test
  void mapperForHolding_verifyPreBuildAverageMerDto() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    final Holding h = mock(Holding.class);

    doCallRealMethod().when(sut).mapperForHolding(any());
    // ACT
    final AverageManagementExpenseCalculationDTO actual = sut.mapperForHolding(h);

    // VERIFY
    verify(sut).preBuildAverageMerDto(h);
    assertSame(merDTO, actual);
  }

}