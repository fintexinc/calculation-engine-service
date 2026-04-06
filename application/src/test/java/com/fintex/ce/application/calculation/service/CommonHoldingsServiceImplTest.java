package com.fintex.ce.application.calculation.service;

import com.fintex.ce.domain.dto.CommonHoldingsDTO;
import com.fintex.ce.domain.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.domain.model.HoldingAggregator;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.TopCommonHoldingsResult;
import com.fintex.ce.domain.model.result.commonholdings.TopCommonHoldingData;
import com.fintex.ce.domain.model.result.correlation.HoldingsKeyResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CommonHoldingsServiceImplTest {

  @Test
  void shouldPerform_whenVerifyCalculateInitialPortfolioWeight() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));

      when(reqDTO.getHoldings()).thenReturn(holdings);
      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
    }
  }

  @Test
  void shouldPerform_whenVerifyLoad() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));
      final var allocations = Map.of(mock(Holding.class), TEN);

      when(reqDTO.getHoldings()).thenReturn(holdings);
      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(fetcher).fetch(eq(holdings), any());
    }
  }

  @Test
  void shouldPerform_whenVerifyGetNumOfFundsMin() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(Map.of());

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).getNumOfFundsMin(reqDTO);
    }
  }

  @Test
  void shouldPerform_whenVerifyverifyGetAccumulativeTypes() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(Map.of());
      when(sut.getNumOfFundsMin(any())).thenReturn(1);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).getAccumulativeTypes(reqDTO);
    }
  }

  @Test
  void shouldPerform_whenVerifyCalculateCalculateTopCommonHoldings() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));

      final var rawCommonHoldings = new com.fintex.ce.domain.model.CommonHoldings();
      rawCommonHoldings.setHoldings(List.of());
      final var holdingsFromSms = Map.of(mock(Holding.class), rawCommonHoldings);
      final var allocations = Map.of(mock(Holding.class), mock(BigDecimal.class));
      final var accumulativeTypes = Set.of("E");
      final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHoldingsDTO.class)));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(holdingsFromSms);
      when(sut.getNumOfFundsMin(any())).thenReturn(1);
      when(sut.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
      when(sut.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).calculateTopCommonHoldings(anyMap(), eq(allocations), eq(accumulativeTypes));
    }
  }

  @Test
  void shouldPerform_whenVerifyCalculateFilterTop10Common() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));

      final var rawCommonHoldings = new com.fintex.ce.domain.model.CommonHoldings();
      rawCommonHoldings.setHoldings(List.of());
      final var holdingsFromSms = Map.of(mock(Holding.class), rawCommonHoldings);
      final var allocations = Map.of(mock(Holding.class), mock(BigDecimal.class));
      final var accumulativeTypes = Set.of("E");
      final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHoldingsDTO.class)));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(holdingsFromSms);
      when(sut.getNumOfFundsMin(any())).thenReturn(1);
      when(sut.getTopCommonHoldingsNumber(any())).thenReturn(123);
      when(sut.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
      when(sut.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).filterTopCommon(1, 123, leaves);
    }
  }

  @Test
  void shouldPerform_whenVerifytoFinalResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));

      final var rawCommonHoldings = new com.fintex.ce.domain.model.CommonHoldings();
      rawCommonHoldings.setHoldings(List.of());
      final var holdingsFromSms = Map.of(mock(Holding.class), rawCommonHoldings);
      final var allocations = Map.of(mock(Holding.class), mock(BigDecimal.class));
      final var accumulativeTypes = Set.of("E");
      final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHoldingsDTO.class)));
      final var sortedLeaves = Map.of(mock(HoldingAggregator.class), TEN);

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(holdingsFromSms);
      when(sut.getNumOfFundsMin(any())).thenReturn(1);
      when(sut.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
      when(sut.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);
      when(sut.filterTopCommon(anyInt(), anyInt(), any())).thenReturn(sortedLeaves);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).toFinalResult(leaves, sortedLeaves);
    }
  }

  @Test
  void shouldPerform_whenCheckResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));

      final var rawCommonHoldings = new com.fintex.ce.domain.model.CommonHoldings();
      rawCommonHoldings.setHoldings(List.of());
      final var holdingsFromSms = Map.of(mock(Holding.class), rawCommonHoldings);
      final var allocations = Map.of(mock(Holding.class), mock(BigDecimal.class));
      final var accumulativeTypes = Set.of("E");
      final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHoldingsDTO.class)));
      final var sortedLeaves = Map.of(mock(HoldingAggregator.class), TEN);
      final var topCommonHoldingsDTO = List.of(mock(TopCommonHoldingData.class));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(holdingsFromSms);
      when(sut.getNumOfFundsMin(any())).thenReturn(1);
      when(sut.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
      when(sut.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);
      when(sut.filterTopCommon(anyInt(), anyInt(), any())).thenReturn(sortedLeaves);
      when(sut.toFinalResult(anyMap(), anyMap())).thenReturn(topCommonHoldingsDTO);

      doCallRealMethod().when(sut).perform(any());
      // ACT
      final TopCommonHoldingsResult actual = sut.perform(reqDTO);

      // VERIFY
      assertEquals(topCommonHoldingsDTO, actual.getCommonHoldings());
    }
  }

  @Test
  void shouldToFinalResult_whenVerifyMapToFinalResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHoldingsDTO.class)));
    final var aggregator = mock(HoldingAggregator.class);
    final var ten = TEN;
    final var sortedLeaves = Map.of(aggregator, ten);
    final var entry = new AbstractMap.SimpleEntry<>(aggregator, ten);

    doCallRealMethod().when(sut).toFinalResult(anyMap(), anyMap());
    // ACT
    var actual = sut.toFinalResult(leaves, sortedLeaves);

    // VERIFY
    verify(sut).mapToFinalResult(leaves, entry);
  }

  @Test
  void shouldToFinalResult_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHoldingsDTO.class)));
    final var sortedLeaves = Map.of(mock(HoldingAggregator.class), TEN);
    final var expected = new TopCommonHoldingData();

    when(sut.mapToFinalResult(anyMap(), any())).thenReturn(expected);

    doCallRealMethod().when(sut).toFinalResult(anyMap(), anyMap());
    // ACT
    final List<TopCommonHoldingData> actual = sut.toFinalResult(leaves, sortedLeaves);

    // VERIFY
    assertEquals(expected, actual.get(0));
  }

  @Test
  void shouldGetNumOfFundsMin_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var topCommonHoldingsReqDTO = mock(TopCommonHoldingsCommand.class);
    final var expected = 1;

    when(topCommonHoldingsReqDTO.getNumOfFundsMin()).thenReturn(null);

    doCallRealMethod().when(sut).getNumOfFundsMin(any());
    // ACT
    final int actual = sut.getNumOfFundsMin(topCommonHoldingsReqDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetNumOfFundsMin_whenCheckResult2() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var topCommonHoldingsReqDTO = mock(TopCommonHoldingsCommand.class);
    final var expected = 7;

    when(topCommonHoldingsReqDTO.getNumOfFundsMin()).thenReturn(expected);

    doCallRealMethod().when(sut).getNumOfFundsMin(any());
    // ACT
    final int actual = sut.getNumOfFundsMin(topCommonHoldingsReqDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetAccumulativeTypes_whenCheckResult() {
    // SETUP
    final var accumulativeTypes = Set.of("E");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulativeTypes));

    final var topCommonHoldingsReqDTO = mock(TopCommonHoldingsCommand.class);

    when(topCommonHoldingsReqDTO.getAccumulateHoldingTypes()).thenReturn(accumulativeTypes);

    doCallRealMethod().when(sut).getAccumulativeTypes(any());
    // ACT
    final Set<String> actual = sut.getAccumulativeTypes(topCommonHoldingsReqDTO);

    // VERIFY
    assertEquals(accumulativeTypes, actual);
  }

  @Test
  void shouldGetAccumulativeTypes_whenCheckResult2() {
    // SETUP
    final var accumulativeTypes = Set.of();
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulativeTypes));

    final var topCommonHoldingsReqDTO = mock(TopCommonHoldingsCommand.class);

    when(topCommonHoldingsReqDTO.getAccumulateHoldingTypes()).thenReturn(Set.of());

    doCallRealMethod().when(sut).getAccumulativeTypes(any());
    // ACT
    final Set<String> actual = sut.getAccumulativeTypes(topCommonHoldingsReqDTO);

    // VERIFY
    assertEquals(accumulativeTypes, actual);

  }

  @Test
  void shouldSecondLevelLeaves_whenCheckResult() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var firstLvlChild = mock(CommonHoldingsDTO.class);

    when(firstLvlChild.getUnderlyingHoldings()).thenReturn(null);

    doCallRealMethod().when(sut).secondLevelLeaves(firstLvlChild);
    // ACT
    final Stream<CommonHoldingsDTO> actual = sut.secondLevelLeaves(firstLvlChild);

    // VERIFY
    assertEquals(firstLvlChild, actual.findFirst().orElseThrow());
  }

  @Test
  void shouldSecondLevelLeaves_whenCheckResult2() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var firstLvlChild = mock(CommonHoldingsDTO.class);
    final var underlyingHolding = mock(CommonHoldingsDTO.class);
    final var underlyingHoldings = List.of(underlyingHolding);

    when(underlyingHolding.getCompanyName()).thenReturn(null);
    when(underlyingHolding.getName()).thenReturn(null);
    when(firstLvlChild.getUnderlyingHoldings()).thenReturn(underlyingHoldings);

    doCallRealMethod().when(sut).secondLevelLeaves(firstLvlChild);
    // ACT
    final Stream<CommonHoldingsDTO> actual = sut.secondLevelLeaves(firstLvlChild);

    // VERIFY
    assertEquals(Stream.of().findFirst(), actual.findFirst());
  }

  @Test
  void shouldFilterTop10Common_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var holdingAggregator = mock(HoldingAggregator.class);
    final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
    final var leaves = Map.of(holdingAggregator, List.of(commonHoldingsDTO));
    final var expected = Map.of(holdingAggregator, TEN);

    when(commonHoldingsDTO.getWeight()).thenReturn(TEN);

    doCallRealMethod().when(sut).filterTopCommon(anyInt(), anyInt(), anyMap());
    // ACT
    final Map<HoldingAggregator, BigDecimal> actual = sut.filterTopCommon(1, 10, leaves);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldSetParentAndCalculateWeight_whenVerifyIsLeafStock() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var allocations = Map.of(new Holding(null, null, null), TEN);
    final var parent = new Holding(null, null, null);
    final var child = mock(CommonHoldingsDTO.class);
    final var expected = new CommonHoldingsDTO();
    expected.setWeight(TEN);
    expected.setHolding(parent);

    when(child.getValue()).thenReturn(TEN);
    when(child.setHolding(parent)).thenReturn(expected);

    doCallRealMethod().when(sut).setParentAndCalculateWeight(anyMap(), any(), any());
    // ACT
    final CommonHoldingsDTO actual = sut.setParentAndCalculateWeight(allocations, parent, child);

    // VERIFY
    verify(sut).isLeafStock(parent, child);
  }

  @Test
  void shouldSetParentAndCalculateWeight_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var allocations = Map.of(new Holding(null, null, null), TEN);
    final var parent = new Holding(null, null, null);
    final var child = mock(CommonHoldingsDTO.class);
    final var expected = new CommonHoldingsDTO();
    expected.setWeight(BigDecimal.valueOf(100));
    expected.setHolding(parent);

    when(sut.isLeafStock(any(), any())).thenReturn(true);
    when(child.getValue()).thenReturn(TEN);
    when(child.setHolding(parent)).thenReturn(expected);

    doCallRealMethod().when(sut).setParentAndCalculateWeight(anyMap(), any(), any());
    // ACT
    final CommonHoldingsDTO actual = sut.setParentAndCalculateWeight(allocations, parent, child);

    // VERIFY
    assertEquals(expected.getWeight(), actual.getWeight());
    assertEquals(expected.getHolding(), actual.getHolding());
  }

  @Test
  void shouldSetParentAndCalculateWeight_whenCheckResult2() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var parent = mock(Holding.class);
    final var allocations = Map.of(parent, TEN);
    final var child = mock(CommonHoldingsDTO.class);
    final var expected = new CommonHoldingsDTO();
    expected.setWeight(BigDecimal.valueOf(100));
    expected.setHolding(parent);

    when(sut.isLeafStock(any(), any())).thenReturn(false);
    when(child.getValue()).thenReturn(TEN);
    when(child.getCompanyName()).thenReturn("Apple Inc");
    when(child.getType()).thenReturn("E");
    when(child.setHolding(parent)).thenReturn(expected);

    doCallRealMethod().when(sut).setParentAndCalculateWeight(anyMap(), any(), any());
    // ACT
    final CommonHoldingsDTO actual = sut.setParentAndCalculateWeight(allocations, parent, child);

    // VERIFY
    assertEquals(expected.getWeight(), actual.getWeight());
    assertEquals(expected.getHolding(), actual.getHolding());
  }

  @Test
  void shouldCalculateWeightWithinSameLeaves_whenVerifyToUserScale() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingsDTO = mock(CommonHoldingsDTO.class);
      final var sameLeaves = List.of(holdingsDTO);
      final var parentHolding = mock(Holding.class);
      final var expected = TEN;

      when(holdingsDTO.getHolding()).thenReturn(parentHolding);
      when(sameLeaves.get(0).getHolding()).thenReturn(parentHolding);
      when(holdingsDTO.getWeight()).thenReturn(expected);

      doCallRealMethod().when(sut).calculateWeightWithinSameLeaves(anyList(), any());
      // ACT
      final BigDecimal actual = sut.calculateWeightWithinSameLeaves(sameLeaves, parentHolding);

      // VERIFY
      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(TEN));
    }
  }

  @Test
  void shouldCalculateWeightWithinSameLeaves_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var holdingsDTO = mock(CommonHoldingsDTO.class);
    final var sameLeaves = List.of(holdingsDTO);
    final var parentHolding = mock(Holding.class);
    final var expected = TEN;

    when(holdingsDTO.getHolding()).thenReturn(parentHolding);
    when(sameLeaves.get(0).getHolding()).thenReturn(parentHolding);
    when(holdingsDTO.getWeight()).thenReturn(expected);

    doCallRealMethod().when(sut).calculateWeightWithinSameLeaves(anyList(), any());
    // ACT
    final BigDecimal actual = sut.calculateWeightWithinSameLeaves(sameLeaves, parentHolding);

    // VERIFY
    assertEquals(expected.doubleValue(), actual.doubleValue());
  }

  @Test
  void shouldMapToFinalResult_whenVerifyBuildDTO() {
    try (var mockedHoldingsKeyResult = Mockito.mockStatic(HoldingsKeyResult.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingAggregator = mock(HoldingAggregator.class);
      final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
      final var leaves = Map.of(holdingAggregator, List.of(commonHoldingsDTO));
      final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregator, TEN);

      final var holdingsKeyDTO = mock(HoldingsKeyResult.class);
      final var holding = mock(Holding.class);

      when(commonHoldingsDTO.getHolding()).thenReturn(holding);
      when(commonHoldingsDTO.getWeight()).thenReturn(TEN);
      when(sut.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
      mockedHoldingsKeyResult.when(() -> HoldingsKeyResult.buildFromHolding(any(), any())).thenReturn(holdingsKeyDTO);

      doCallRealMethod().when(sut).mapToFinalResult(anyMap(), any());
      // ACT
      sut.mapToFinalResult(leaves, sortedLeafEntry);

      // VERIFY
      mockedHoldingsKeyResult.verify(() -> HoldingsKeyResult.buildFromHolding(holding, TEN));
    }
  }

  @Test
  void shouldMapToFinalResult_whenVerifyCalculateWeightWithinSameLeaves() {
    try (var mockedHoldingsKeyResult = Mockito.mockStatic(HoldingsKeyResult.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingAggregator = mock(HoldingAggregator.class);
      final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
      final var leaves = Map.of(holdingAggregator, List.of(commonHoldingsDTO));
      final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregator, TEN);

      final var holdingsKeyDTO = mock(HoldingsKeyResult.class);
      final var holding = mock(Holding.class);

      when(commonHoldingsDTO.getHolding()).thenReturn(holding);
      when(commonHoldingsDTO.getWeight()).thenReturn(TEN);
      mockedHoldingsKeyResult.when(() -> HoldingsKeyResult.buildFromHolding(any(), any())).thenReturn(holdingsKeyDTO);

      doCallRealMethod().when(sut).mapToFinalResult(anyMap(), any());
      // ACT
      sut.mapToFinalResult(leaves, sortedLeafEntry);

      // VERIFY
      verify(sut).calculateWeightWithinSameLeaves(List.of(commonHoldingsDTO), holding);
    }
  }

  @Test
  void shouldMapToFinalResult_whenCheckResult() {
    try (var mockedHoldingsKeyResult = Mockito.mockStatic(HoldingsKeyResult.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingAggregator = new HoldingAggregator("Tesla", null, null);
      final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
      final var leaves = Map.of(holdingAggregator, List.of(commonHoldingsDTO));
      final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregator, TEN);

      final var holdingsKeyDTO = mock(HoldingsKeyResult.class);
      final var holding = mock(Holding.class);
      final var expected = new TopCommonHoldingData("Tesla", null, null, null, toUserScale(TEN), 1, Set.of(
          holdingsKeyDTO));

      when(commonHoldingsDTO.getHolding()).thenReturn(holding);
      when(commonHoldingsDTO.getWeight()).thenReturn(TEN);
      when(sut.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
      mockedHoldingsKeyResult.when(() -> HoldingsKeyResult.buildFromHolding(any(), any())).thenReturn(holdingsKeyDTO);

      doCallRealMethod().when(sut).mapToFinalResult(anyMap(), any());
      // ACT
      final TopCommonHoldingData actual = sut.mapToFinalResult(leaves, sortedLeafEntry);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldMapToFinalResult_whenCheckResult2() {
    try (var mockedHoldingsKeyResult = Mockito.mockStatic(HoldingsKeyResult.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingAggregator = new HoldingAggregator("Tesla", null, null);
      final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
      final var leaves = Map.of(holdingAggregator, List.of(commonHoldingsDTO));
      final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregator, TEN);

      final var holdingsKeyDTO = mock(HoldingsKeyResult.class);
      final var holding = mock(Holding.class);
      final var expected = new TopCommonHoldingData("Tesla", "H", null, null, toUserScale(TEN), 1, Set.of(
          holdingsKeyDTO));

      when(commonHoldingsDTO.getTicker()).thenReturn("H");
      when(commonHoldingsDTO.getHolding()).thenReturn(holding);
      when(commonHoldingsDTO.getWeight()).thenReturn(TEN);
      when(sut.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
      mockedHoldingsKeyResult.when(() -> HoldingsKeyResult.buildFromHolding(any(), any())).thenReturn(holdingsKeyDTO);

      doCallRealMethod().when(sut).mapToFinalResult(anyMap(), any());
      // ACT
      final TopCommonHoldingData actual = sut.mapToFinalResult(leaves, sortedLeafEntry);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldCalculateTopCommonHoldings_whenVerifyFirstLevelLeaves() {
    // SETUP
    final var accumulativeTypes = Set.of("E");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulativeTypes));

    final var holding = mock(Holding.class);
    final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
    final var holdings = Map.of(holding, List.of(commonHoldingsDTO));
    final var allocations = Map.of(holding, TEN);

    doCallRealMethod().when(sut).calculateTopCommonHoldings(anyMap(), anyMap(), anySet());
    // ACT
    sut.calculateTopCommonHoldings(holdings, allocations, accumulativeTypes);

    // VERIFY
    verify(sut).firstLevelLeaves(allocations, holding, List.of(commonHoldingsDTO));
  }

  @Test
  void shouldCalculateTopCommonHoldings_whenVerifySecondLevelLeaves() {
    // SETUP
    final var accumulativeTypes = Set.of("E");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulativeTypes));

    final var holding = mock(Holding.class);
    final var commonHoldingsDTO = mock(CommonHoldingsDTO.class);
    final var holdings = Map.of(holding, List.of(commonHoldingsDTO));
    final var allocations = Map.of(holding, TEN);

    when(sut.firstLevelLeaves(anyMap(), any(), anyList())).thenReturn(Stream.of(commonHoldingsDTO));

    doCallRealMethod().when(sut).calculateTopCommonHoldings(anyMap(), anyMap(), anySet());
    // ACT
    sut.calculateTopCommonHoldings(holdings, allocations, accumulativeTypes);

    // VERIFY
    verify(sut).secondLevelLeaves(commonHoldingsDTO);
  }

  @Test
  void shouldFirstLevelLeaves_whenVerifySetParentAndCalculateWeight() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var allocations = Map.of(mock(Holding.class), TEN);
    final var parent = mock(Holding.class);
    final var child = mock(CommonHoldingsDTO.class);
    final var firstLevelChildren = List.of(child);

    when(child.getType()).thenReturn("E");
    when(child.getUnderlyingHoldings()).thenReturn(null);
    when(sut.setParentAndCalculateWeight(anyMap(), any(), any())).thenReturn(child);

    doCallRealMethod().when(sut).firstLevelLeaves(anyMap(), any(), anyList());
    // ACT
    final Stream<CommonHoldingsDTO> actual = sut.firstLevelLeaves(allocations, parent, firstLevelChildren);

    // VERIFY
    assertEquals(1, actual.toList().size());
    verify(sut).setParentAndCalculateWeight(allocations, parent, child);
  }

  @Test
  void shouldFirstLevelLeaves_whenCheckResult() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var allocations = Map.of(mock(Holding.class), TEN);
    final var parent = mock(Holding.class);
    final var child = mock(CommonHoldingsDTO.class);
    final var firstLevelChildren = List.of(child);
    final var expected = new CommonHoldingsDTO("Apple Inc", null, TEN, null, null);

    when(child.getType()).thenReturn("E");
    when(child.getCompanyName()).thenReturn("Apple Inc");
    when(child.getValue()).thenReturn(TEN);
    when(child.getUnderlyingHoldings()).thenReturn(null);
    when(sut.setParentAndCalculateWeight(anyMap(), any(), any())).thenReturn(child);

    doCallRealMethod().when(sut).firstLevelLeaves(anyMap(), any(), anyList());
    // ACT
    final Stream<CommonHoldingsDTO> actual = sut.firstLevelLeaves(allocations, parent, firstLevelChildren);

    // VERIFY
    assertEquals(expected.getWeight(), actual.findFirst().orElseThrow().getWeight());
  }

  @Test
  void shouldSetParentAndCalculateWeightSecondLvlLeaf_whenCheckResult() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      // SETUP
      final var accumulateTypes = Set.of("FE");
      final var fetcher = mock(SecurityDataFetcher.class);
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, accumulateTypes));

      final var firstLvlParent = mock(CommonHoldingsDTO.class);
      final var child = mock(CommonHoldingsDTO.class);

      when(child.setHolding(any())).thenReturn(child);
      when(child.setWeight(any())).thenReturn(child);
      when(child.getValue()).thenReturn(TEN);
      when(child.getWeight()).thenReturn(HUNDRED);
      when(firstLvlParent.getWeight()).thenReturn(TEN);
      mockedDecimalUtils.when(() -> DecimalUtils.toUserScale(TEN)).thenReturn(TEN);
      doCallRealMethod().when(sut).setParentAndCalculateWeightSecondLvlLeaf(any(), any());
      // ACT
      final CommonHoldingsDTO actual = sut.setParentAndCalculateWeightSecondLvlLeaf(firstLvlParent, child);

      // VERIFY
      assertEquals(HUNDRED, actual.getWeight());
    }
  }

  @Test
  void shouldIsLeafStock_whenCheckResult() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var parent = mock(Holding.class);
    final var child = mock(CommonHoldingsDTO.class);

    when(parent.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_CANADA);
    when(child.getCompanyName()).thenReturn("test");
    when(child.getType()).thenReturn("E");

    doCallRealMethod().when(sut).isLeafStock(any(), any());
    // ACT
    final boolean actual = sut.isLeafStock(parent, child);

    // VERIFY
    assertTrue(actual);
  }

  @Test
  void shouldIsLeafStock_whenCheckResult2() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var parent = mock(Holding.class);
    final var child = mock(CommonHoldingsDTO.class);

    when(parent.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_CANADA);
    when(child.getCompanyName()).thenReturn("test");
    when(child.getType()).thenReturn("FE");

    doCallRealMethod().when(sut).isLeafStock(any(), any());
    // ACT
    final boolean actual = sut.isLeafStock(parent, child);

    // VERIFY
    assertFalse(actual);
  }

  @Test
  void shouldGetTopCommonHoldingsNumber_whenReturnDefault10WhenGetNumOfTopCommonHoldingsIsNull() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final TopCommonHoldingsCommand req = mock(TopCommonHoldingsCommand.class);
    doReturn(null).when(req).getNumOfTopCommonHoldings();

    doCallRealMethod().when(sut).getTopCommonHoldingsNumber(req);
    // ACT
    final int actual = sut.getTopCommonHoldingsNumber(req);

    // VERIFY
    final int expected = 10;
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetTopCommonHoldingsNumber_whenReturnProvidedNumberIfNotNull() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final TopCommonHoldingsCommand req = mock(TopCommonHoldingsCommand.class);
    doReturn(11).when(req).getNumOfTopCommonHoldings();

    doCallRealMethod().when(sut).getTopCommonHoldingsNumber(req);
    // ACT
    final int actual = sut.getTopCommonHoldingsNumber(req);

    // VERIFY
    final int expected = 11;
    assertEquals(expected, actual);
  }

  @Test
  void shouldPerform_whenVerifyGetTopCommonHoldingsNumber() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var sut = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var reqDTO = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(Holding.class));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(Map.of());

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).getTopCommonHoldingsNumber(reqDTO);
    }
  }
}